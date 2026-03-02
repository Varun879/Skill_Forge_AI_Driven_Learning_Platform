import express from "express";
import { createServer as createViteServer } from "vite";
import dotenv from "dotenv";
import path from "path";
import { fileURLToPath } from "url";
import sqlite3 from "better-sqlite3";
import bcrypt from "bcryptjs";
import { SignJWT, jwtVerify } from "jose";
import { GoogleGenAI } from "@google/genai";
import multer from "multer";
import fs from "fs";

import axios from "axios";

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;

app.use(express.json());
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

// Database setup
const db = new sqlite3("skillforge.db");

// Initialize tables
db.exec(`
  DROP TABLE IF EXISTS subtopic_problems;
  DROP TABLE IF EXISTS roadmap_subtopics;
  DROP TABLE IF EXISTS roadmap_topics;
  DROP TABLE IF EXISTS problems;
  DROP TABLE IF EXISTS roadmap_nodes;

  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'Learner',
    profile_image TEXT,
    mastery_percent INTEGER DEFAULT 0,
    accuracy INTEGER DEFAULT 0,
    avg_solve_time TEXT DEFAULT '00:00',
    streak INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tutor_id INTEGER,
    title TEXT NOT NULL,
    description TEXT,
    difficulty TEXT,
    tags TEXT,
    thumbnail TEXT,
    price REAL DEFAULT 0,
    status TEXT DEFAULT 'draft',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(tutor_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS course_modules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER,
    title TEXT NOT NULL,
    video_url TEXT,
    notes TEXT,
    order_index INTEGER,
    FOREIGN KEY(course_id) REFERENCES courses(id)
  );

  CREATE TABLE IF NOT EXISTS problems (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    topic_tags TEXT NOT NULL,
    description TEXT NOT NULL,
    constraints TEXT,
    edge_cases TEXT,
    samples TEXT NOT NULL,
    tests TEXT NOT NULL,
    ai_hint_seed TEXT,
    mastery_impact INTEGER DEFAULT 5,
    estimated_time TEXT DEFAULT '15 mins',
    module_id INTEGER,
    tutor_id INTEGER,
    FOREIGN KEY(module_id) REFERENCES course_modules(id),
    FOREIGN KEY(tutor_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS enrollments (
    user_id INTEGER,
    course_id INTEGER,
    enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id, course_id),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(course_id) REFERENCES courses(id)
  );

  CREATE TABLE IF NOT EXISTS doubts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    course_id INTEGER,
    problem_id TEXT,
    question TEXT NOT NULL,
    answer TEXT,
    status TEXT DEFAULT 'open',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(course_id) REFERENCES courses(id),
    FOREIGN KEY(problem_id) REFERENCES problems(id)
  );

  CREATE TABLE IF NOT EXISTS submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    problem_id TEXT,
    code TEXT,
    language TEXT,
    status TEXT,
    score INTEGER,
    feedback TEXT,
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(problem_id) REFERENCES problems(id)
  );

  CREATE TABLE IF NOT EXISTS roadmap_topics (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    order_index INTEGER
  );

  CREATE TABLE IF NOT EXISTS roadmap_subtopics (
    id TEXT PRIMARY KEY,
    topic_id TEXT,
    title TEXT NOT NULL,
    description TEXT,
    locked BOOLEAN DEFAULT 0,
    FOREIGN KEY(topic_id) REFERENCES roadmap_topics(id)
  );

  CREATE TABLE IF NOT EXISTS subtopic_problems (
    subtopic_id TEXT,
    problem_id TEXT,
    PRIMARY KEY(subtopic_id, problem_id),
    FOREIGN KEY(subtopic_id) REFERENCES roadmap_subtopics(id),
    FOREIGN KEY(problem_id) REFERENCES problems(id)
  );
`);

// Seed initial data if empty
const problemCount = db.prepare("SELECT COUNT(*) as count FROM problems").get() as { count: number };
if (problemCount.count === 0) {
  const insertProblem = db.prepare(`
    INSERT INTO problems (id, title, difficulty, topic_tags, description, constraints, edge_cases, samples, tests, ai_hint_seed, mastery_impact, estimated_time)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);

  insertProblem.run(
    "p1",
    "Two Sum",
    "Easy",
    "Arrays, Hash-Table",
    "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nIn the real world, this is similar to finding a pair of items in a store that exactly match a gift card balance.\n\n### Input Format\n- `nums`: An array of integers\n- `target`: A single integer\n\n### Output Format\n- An array of two integers representing the indices.",
    JSON.stringify([
      "2 ≤ nums.length ≤ 10^4",
      "-10^9 ≤ nums[i] ≤ 10^9",
      "-10^9 ≤ target ≤ 10^9",
      "Time complexity target: O(n)"
    ]),
    JSON.stringify([
      "Empty array (should handle as error or 0)",
      "Array with only two elements",
      "Large negative and positive values",
      "Duplicate values in the array"
    ]),
    JSON.stringify([
      { input: "[2, 7, 11, 15], 9", output: "[0, 1]", explanation: "Because nums[0] + nums[1] == 9, we return [0, 1]." }
    ]),
    JSON.stringify([{ input: "[2,7,11,15], 9", output: "[0,1]" }]),
    "Think about using a hash map to store the complement of each number. This allows you to find the target in a single pass.",
    5,
    "15 mins"
  );

  insertProblem.run(
    "p2",
    "Maximum Subarray",
    "Medium",
    "Arrays, DP",
    "Given an integer array `nums`, find the subarray with the largest sum, and return its sum.\n\nThis is a classic problem used in genomic sequence analysis to find the most 'active' region of a DNA strand.\n\n### Input Format\n- `nums`: An array of integers\n\n### Output Format\n- A single integer representing the maximum sum.",
    JSON.stringify([
      "1 ≤ nums.length ≤ 10^5",
      "-10^4 ≤ nums[i] ≤ 10^4",
      "Time complexity target: O(n)"
    ]),
    JSON.stringify([
      "All negative numbers",
      "Single element array",
      "Alternating positive and negative numbers",
      "Very large array with 10^5 elements"
    ]),
    JSON.stringify([
      { input: "[-2, 1, -3, 4, -1, 2, 1, -5, 4]", output: "6", explanation: "The subarray [4, -1, 2, 1] has the largest sum = 6." }
    ]),
    JSON.stringify([{ input: "[-2,1,-3,4,-1,2,1,-5,4]", output: "6" }]),
    "Kadane's algorithm is a great way to solve this in linear time. Keep track of the current maximum sum ending at each position.",
    10,
    "25 mins"
  );

  insertProblem.run(
    "p3",
    "Fibonacci Number",
    "Easy",
    "Recursion, DP",
    "The Fibonacci numbers, commonly denoted `F(n)` form a sequence, called the Fibonacci sequence, such that each number is the sum of the two preceding ones, starting from 0 and 1.\n\nFibonacci numbers appear in nature, such as the arrangement of leaves on a stem or the flowering of an artichoke.\n\n### Input Format\n- `n`: A non-negative integer\n\n### Output Format\n- A single integer representing F(n).",
    JSON.stringify([
      "0 ≤ n ≤ 30",
      "Time complexity target: O(n)"
    ]),
    JSON.stringify([
      "n = 0",
      "n = 1",
      "n = 30 (maximum constraint)"
    ]),
    JSON.stringify([
      { input: "2", output: "1", explanation: "F(2) = F(1) + F(0) = 1 + 0 = 1." }
    ]),
    JSON.stringify([{ input: "2", output: "1" }]),
    "You can use recursion with memoization to optimize the calculation, or use an iterative approach to achieve O(n) time complexity and O(1) space complexity.",
    3,
    "10 mins"
  );

  // Seed Roadmap
  const insertTopic = db.prepare("INSERT INTO roadmap_topics (id, title, description, icon, order_index) VALUES (?, ?, ?, ?, ?)");
  const insertSubtopic = db.prepare("INSERT INTO roadmap_subtopics (id, topic_id, title, description, locked) VALUES (?, ?, ?, ?, ?)");
  const insertSubtopicProblem = db.prepare("INSERT INTO subtopic_problems (subtopic_id, problem_id) VALUES (?, ?)");

  insertTopic.run("t1", "Data Structures", "Master the fundamental building blocks of algorithms.", "Code2", 1);
  insertTopic.run("t2", "Algorithms", "Learn advanced techniques for solving complex problems.", "Zap", 2);

  insertSubtopic.run("s1", "t1", "Arrays & Hashing", "The foundation of most coding problems.", 0);
  insertSubtopic.run("s2", "t1", "Linked Lists", "Understanding dynamic memory and pointers.", 1);
  insertSubtopic.run("s3", "t2", "Dynamic Programming", "Solving problems by breaking them into subproblems.", 1);

  insertSubtopicProblem.run("s1", "p1");
  insertSubtopicProblem.run("s1", "p2");
  insertSubtopicProblem.run("s3", "p3");
}

// Add profile_image column if it doesn't exist
try {
  db.prepare("ALTER TABLE users ADD COLUMN profile_image TEXT").run();
} catch (e) {
  // Column already exists or other error
}

// Multer setup for profile images
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = path.join(__dirname, "uploads/profiles");
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
    cb(null, "profile-" + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB
  fileFilter: (req, file, cb) => {
    const allowedTypes = ["image/jpeg", "image/png", "image/webp"];
    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error("Invalid file type. Only JPG, PNG and WEBP are allowed."));
    }
  }
});

// Auth Middleware
const JWT_SECRET = new TextEncoder().encode(process.env.JWT_SECRET || "skillforge-secret-key-123");

async function authenticate(req: any, res: any, next: any) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(401).json({ error: "Unauthorized" });
  }
  const token = authHeader.split(" ")[1];
  try {
    const { payload } = await jwtVerify(token, JWT_SECRET);
    req.user = payload;
    next();
  } catch (err) {
    res.status(401).json({ error: "Invalid token" });
  }
}

// API Routes
app.get("/api/auth/google/url", (req, res) => {
  const role = req.query.role || 'Learner';
  const redirectUri = `${process.env.APP_URL}/api/auth/google/callback`;
  
  const params = new URLSearchParams({
    client_id: process.env.GOOGLE_CLIENT_ID!,
    redirect_uri: redirectUri,
    response_type: 'code',
    scope: 'openid email profile',
    state: JSON.stringify({ role }),
    access_type: 'offline',
    prompt: 'consent'
  });

  const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?${params}`;
  res.json({ url: authUrl });
});

app.get("/api/auth/google/callback", async (req, res) => {
  const { code, state } = req.query;
  const { role } = JSON.parse(state as string || '{"role":"Learner"}');
  const redirectUri = `${process.env.APP_URL}/api/auth/google/callback`;

  try {
    // Exchange code for tokens
    const tokenRes = await axios.post('https://oauth2.googleapis.com/token', {
      code,
      client_id: process.env.GOOGLE_CLIENT_ID,
      client_secret: process.env.GOOGLE_CLIENT_SECRET,
      redirect_uri: redirectUri,
      grant_type: 'authorization_code',
    });

    const { access_token } = tokenRes.data;

    // Get user info
    const userRes = await axios.get('https://www.googleapis.com/oauth2/v2/userinfo', {
      headers: { Authorization: `Bearer ${access_token}` }
    });

    const { email, name, picture } = userRes.data;

    // Check if user exists
    let user = db.prepare("SELECT * FROM users WHERE email = ?").get(email) as any;

    if (!user) {
      // Create new user
      const dummyPassword = await bcrypt.hash(Math.random().toString(36), 10);
      const result = db.prepare("INSERT INTO users (name, email, password, role, profile_image) VALUES (?, ?, ?, ?, ?)").run(name, email, dummyPassword, role, picture);
      user = { id: result.lastInsertRowid, name, email, role, profile_image: picture };
    }

    const token = await new SignJWT({ id: user.id, email: user.email, role: user.role })
      .setProtectedHeader({ alg: "HS256" })
      .setExpirationTime("24h")
      .sign(JWT_SECRET);

    const userData = JSON.stringify({ id: user.id, name: user.name, email: user.email, role: user.role, profile_image: user.profile_image });

    res.send(`
      <html>
        <body>
          <script>
            if (window.opener) {
              window.opener.postMessage({ 
                type: 'OAUTH_AUTH_SUCCESS', 
                token: '${token}', 
                user: ${userData} 
              }, '*');
              window.close();
            } else {
              window.location.href = '/';
            }
          </script>
          <p>Authentication successful. This window should close automatically.</p>
        </body>
      </html>
    `);
  } catch (err) {
    console.error('Google OAuth Error:', err);
    res.status(500).send('Authentication failed');
  }
});

app.post("/api/auth/signup", async (req, res) => {
  const { name, email, password, role } = req.body;
  const hashedPassword = await bcrypt.hash(password, 10);
  try {
    const result = db.prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)").run(name, email, hashedPassword, role);
    const token = await new SignJWT({ id: result.lastInsertRowid, email, role })
      .setProtectedHeader({ alg: "HS256" })
      .setExpirationTime("24h")
      .sign(JWT_SECRET);
    res.json({ token, user: { id: result.lastInsertRowid, name, email, role } });
  } catch (err: any) {
    res.status(400).json({ error: "Email already exists" });
  }
});

app.post("/api/auth/login", async (req, res) => {
  const { email, password } = req.body;
  const user = db.prepare("SELECT * FROM users WHERE email = ?").get(email) as any;
  if (!user || !(await bcrypt.compare(password, user.password))) {
    return res.status(401).json({ error: "Invalid credentials" });
  }
  const token = await new SignJWT({ id: user.id, email: user.email, role: user.role })
    .setProtectedHeader({ alg: "HS256" })
    .setExpirationTime("24h")
    .sign(JWT_SECRET);
  res.json({ token, user: { id: user.id, name: user.name, email: user.email, role: user.role } });
});

app.get("/api/user/me", authenticate, (req: any, res) => {
  const user = db.prepare("SELECT id, name, email, role, profile_image, mastery_percent, accuracy, avg_solve_time, streak FROM users WHERE id = ?").get(req.user.id);
  res.json(user);
});

app.post("/api/user/upload-profile-image", authenticate, upload.single("image"), (req: any, res) => {
  if (!req.file) {
    return res.status(400).json({ error: "No file uploaded" });
  }
  
  const imageUrl = `/uploads/profiles/${req.file.filename}`;
  try {
    db.prepare("UPDATE users SET profile_image = ? WHERE id = ?").run(imageUrl, req.user.id);
    res.json({ success: true, imageUrl });
  } catch (err) {
    res.status(500).json({ error: "Failed to update profile image" });
  }
});

app.put("/api/user/me", authenticate, (req: any, res) => {
  const { name, email } = req.body;
  try {
    db.prepare("UPDATE users SET name = ?, email = ? WHERE id = ?").run(name, email, req.user.id);
    res.json({ success: true });
  } catch (err) {
    res.status(400).json({ error: "Email already exists or invalid data" });
  }
});

app.post("/api/user/change-password", authenticate, async (req: any, res) => {
  const { current, new: newPassword } = req.body;
  const user = db.prepare("SELECT password FROM users WHERE id = ?").get(req.user.id) as any;
  
  if (!user || !(await bcrypt.compare(current, user.password))) {
    return res.status(401).json({ error: "Current password incorrect" });
  }
  
  const hashedPassword = await bcrypt.hash(newPassword, 10);
  db.prepare("UPDATE users SET password = ? WHERE id = ?").run(hashedPassword, req.user.id);
  res.json({ success: true });
});

app.delete("/api/user/me", authenticate, (req: any, res) => {
  const userId = req.user.id;
  const userRole = req.user.role;

  try {
    db.transaction(() => {
      if (userRole === 'Tutor') {
        const courses = db.prepare("SELECT id FROM courses WHERE tutor_id = ?").all(userId) as { id: number }[];
        const courseIds = courses.map(c => c.id);

        if (courseIds.length > 0) {
          const placeholders = courseIds.map(() => '?').join(',');
          db.prepare(`DELETE FROM doubts WHERE course_id IN (${placeholders})`).run(...courseIds);
          db.prepare(`DELETE FROM enrollments WHERE course_id IN (${placeholders})`).run(...courseIds);

          const modules = db.prepare(`SELECT id FROM course_modules WHERE course_id IN (${placeholders})`).all(...courseIds) as { id: number }[];
          const moduleIds = modules.map(m => m.id);
          
          if (moduleIds.length > 0) {
             const modPlaceholders = moduleIds.map(() => '?').join(',');
             db.prepare(`DELETE FROM submissions WHERE problem_id IN (SELECT id FROM problems WHERE module_id IN (${modPlaceholders}) OR tutor_id = ?)`).run(...moduleIds, userId);
             db.prepare(`DELETE FROM subtopic_problems WHERE problem_id IN (SELECT id FROM problems WHERE module_id IN (${modPlaceholders}) OR tutor_id = ?)`).run(...moduleIds, userId);
             db.prepare(`DELETE FROM problems WHERE module_id IN (${modPlaceholders}) OR tutor_id = ?`).run(...moduleIds, userId);
             db.prepare(`DELETE FROM course_modules WHERE course_id IN (${placeholders})`).run(...courseIds);
          } else {
             db.prepare(`DELETE FROM submissions WHERE problem_id IN (SELECT id FROM problems WHERE tutor_id = ?)`).run(userId);
             db.prepare(`DELETE FROM subtopic_problems WHERE problem_id IN (SELECT id FROM problems WHERE tutor_id = ?)`).run(userId);
             db.prepare(`DELETE FROM problems WHERE tutor_id = ?`).run(userId);
          }
          db.prepare(`DELETE FROM courses WHERE tutor_id = ?`).run(userId);
        } else {
          db.prepare(`DELETE FROM submissions WHERE problem_id IN (SELECT id FROM problems WHERE tutor_id = ?)`).run(userId);
          db.prepare(`DELETE FROM subtopic_problems WHERE problem_id IN (SELECT id FROM problems WHERE tutor_id = ?)`).run(userId);
          db.prepare(`DELETE FROM problems WHERE tutor_id = ?`).run(userId);
        }
      }

      db.prepare("DELETE FROM enrollments WHERE user_id = ?").run(userId);
      db.prepare("DELETE FROM doubts WHERE user_id = ?").run(userId);
      db.prepare("DELETE FROM submissions WHERE user_id = ?").run(userId);
      db.prepare("DELETE FROM users WHERE id = ?").run(userId);
    })();
    res.json({ success: true });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Failed to delete account" });
  }
});

app.get("/api/user/metrics", authenticate, (req: any, res) => {
  const { range } = req.query;
  const user = db.prepare("SELECT mastery_percent, accuracy, avg_solve_time, streak FROM users WHERE id = ?").get(req.user.id) as any;
  
  const now = new Date();
  let dataPoints: any[] = [];

  if (range === '7d') {
    // Last 7 days
    for (let i = 6; i >= 0; i--) {
      const d = new Date(now);
      d.setDate(d.getDate() - i);
      const label = d.toLocaleDateString('en-US', { weekday: 'short' });
      dataPoints.push({
        label,
        problems: Math.round(Math.random() * 5),
        accuracy: 60 + Math.round(Math.random() * 30)
      });
    }
  } else if (range === '30d') {
    // Last 30 days (grouped by week or just 4 points)
    for (let i = 3; i >= 0; i--) {
      const d = new Date(now);
      d.setDate(d.getDate() - (i * 7));
      const label = `Week ${4-i}`;
      dataPoints.push({
        label,
        problems: 5 + Math.round(Math.random() * 15),
        accuracy: 65 + Math.round(Math.random() * 25)
      });
    }
  } else if (range === '3m' || range === '6m') {
    // Last 3 or 6 months
    const monthsCount = range === '3m' ? 3 : 6;
    for (let i = monthsCount - 1; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const label = d.toLocaleDateString('en-US', { month: 'short' });
      dataPoints.push({
        label,
        problems: 10 + Math.round(Math.random() * 40),
        accuracy: 70 + Math.round(Math.random() * 20)
      });
    }
  } else {
    // All Time (Default to last 12 months)
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const label = d.toLocaleDateString('en-US', { month: 'short' });
      dataPoints.push({
        label,
        problems: 5 + Math.round(Math.random() * 50),
        accuracy: 60 + Math.round(Math.random() * 35)
      });
    }
  }

  const topicDistribution = [
    { name: 'Arrays', value: 40 },
    { name: 'DP', value: 15 },
    { name: 'Graphs', value: 25 },
    { name: 'Recursion', value: 20 },
  ];

  const recentActivity = db.prepare(`
    SELECT p.title, s.status, s.created_at as time, s.score
    FROM submissions s
    JOIN problems p ON s.problem_id = p.id
    WHERE s.user_id = ?
    ORDER BY s.created_at DESC LIMIT 5
  `).all(req.user.id);

  res.json({
    masteryPercent: user.mastery_percent,
    accuracy: user.accuracy,
    avgSolveTime: user.avg_solve_time,
    currentStreakDays: user.streak,
    chartData: dataPoints,
    topicDistribution,
    recentActivity,
    weekly: [
      { day: "Mon", solved: 2, attempted: 3 },
      { day: "Tue", solved: 4, attempted: 5 },
      { day: "Wed", solved: 1, attempted: 2 },
      { day: "Thu", solved: 3, attempted: 3 },
      { day: "Fri", solved: 5, attempted: 6 },
      { day: "Sat", solved: 0, attempted: 0 },
      { day: "Sun", solved: 1, attempted: 1 },
    ]
  });
});

app.get("/api/user/export-report", authenticate, (req: any, res) => {
  const { range } = req.query;
  const user = db.prepare("SELECT name, email, mastery_percent, accuracy, streak FROM users WHERE id = ?").get(req.user.id) as any;
  
  const submissions = db.prepare(`
    SELECT p.title, s.status, s.score, s.language, s.created_at
    FROM submissions s
    JOIN problems p ON s.problem_id = p.id
    WHERE s.user_id = ?
    ORDER BY s.created_at DESC
  `).all(req.user.id) as any[];

  // Generate CSV content
  let csv = `SkillForge Performance Report\n`;
  csv += `User: ${user.name} (${user.email})\n`;
  csv += `Date: ${new Date().toLocaleDateString()}\n`;
  csv += `Time Range: ${range || 'All Time'}\n\n`;
  
  csv += `Summary Statistics\n`;
  csv += `Mastery,${user.mastery_percent}%\n`;
  csv += `Accuracy,${user.accuracy}%\n`;
  csv += `Current Streak,${user.streak} days\n\n`;
  
  csv += `Recent Submissions\n`;
  csv += `Problem,Status,Score,Language,Date\n`;
  submissions.forEach(s => {
    csv += `"${s.title}",${s.status},${s.score},${s.language},${s.created_at}\n`;
  });

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', `attachment; filename=skillforge-analytics-report-${Date.now()}.csv`);
  res.status(200).send(csv);
});

app.get("/api/problems", authenticate, (req, res) => {
  const problems = db.prepare("SELECT id, title, difficulty, topic_tags, description, constraints, mastery_impact, estimated_time FROM problems").all();
  res.json(problems.map((p: any) => ({
    ...p,
    topicTags: p.topic_tags.split(","),
    constraints: p.constraints ? JSON.parse(p.constraints) : [],
    masteryImpact: p.mastery_impact,
    estimatedTime: p.estimated_time
  })));
});

app.get("/api/problems/:id", authenticate, (req, res) => {
  const problem = db.prepare("SELECT * FROM problems WHERE id = ?").get(req.params.id) as any;
  if (!problem) return res.status(404).json({ error: "Problem not found" });
  res.json({
    ...problem,
    topicTags: problem.topic_tags.split(","),
    constraints: problem.constraints ? JSON.parse(problem.constraints) : [],
    edgeCases: problem.edge_cases ? JSON.parse(problem.edge_cases) : [],
    samples: JSON.parse(problem.samples),
    tests: JSON.parse(problem.tests),
    masteryImpact: problem.mastery_impact,
    estimatedTime: problem.estimated_time
  });
});

app.get("/api/roadmap", authenticate, (req, res) => {
  const topics = db.prepare("SELECT * FROM roadmap_topics ORDER BY order_index").all() as any[];
  const roadmap = topics.map(topic => {
    const subtopics = db.prepare("SELECT * FROM roadmap_subtopics WHERE topic_id = ?").all(topic.id) as any[];
    const subtopicsWithProblems = subtopics.map(sub => {
      const problems = db.prepare(`
        SELECT p.id, p.title, p.difficulty 
        FROM problems p 
        JOIN subtopic_problems sp ON p.id = sp.problem_id 
        WHERE sp.subtopic_id = ?
      `).all(sub.id);
      return { ...sub, problems };
    });
    return { ...topic, subtopics: subtopicsWithProblems };
  });
  res.json(roadmap);
});

app.post("/api/submission", authenticate, (req: any, res) => {
  const { problemId, code, lang } = req.body;
  // Mock execution
  const status = Math.random() > 0.3 ? "Accepted" : "Wrong Answer";
  const score = status === "Accepted" ? 100 : 0;
  
  db.prepare("INSERT INTO submissions (user_id, problem_id, code, language, status, score) VALUES (?, ?, ?, ?, ?, ?)")
    .run(req.user.id, problemId, code, lang, status, score);
    
  if (status === "Accepted") {
    db.prepare("UPDATE users SET mastery_percent = MIN(100, mastery_percent + 2), accuracy = MIN(100, accuracy + 1) WHERE id = ?")
      .run(req.user.id);
  }
  
  res.json({ status, score, testResults: [{ name: "Test 1", passed: status === "Accepted" }] });
});

app.post("/api/ai/hint", authenticate, async (req, res) => {
  const { problemId } = req.body;
  const problem = db.prepare("SELECT title, description, ai_hint_seed FROM problems WHERE id = ?").get(problemId) as any;
  
  if (!process.env.GEMINI_API_KEY) {
    return res.json({ hint: problem.ai_hint_seed || "Try to break the problem into smaller subproblems." });
  }

  try {
    const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
    const response = await ai.models.generateContent({
      model: "gemini-3-flash-preview",
      contents: `Give a helpful coding hint for the problem "${problem.title}": ${problem.description}. Seed: ${problem.ai_hint_seed}. Don't give the full solution, just a nudge in the right direction.`,
    });
    res.json({ hint: response.text });
  } catch (err) {
    res.json({ hint: problem.ai_hint_seed || "Think about the edge cases." });
  }
});

// Tutor API Routes
app.get("/api/tutor/dashboard", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  
  const totalCourses = db.prepare("SELECT COUNT(*) as count FROM courses WHERE tutor_id = ?").get(req.user.id) as any;
  const totalEnrolled = db.prepare(`
    SELECT COUNT(*) as count FROM enrollments e
    JOIN courses c ON e.course_id = c.id
    WHERE c.tutor_id = ?
  `).get(req.user.id) as any;
  const activeDoubts = db.prepare(`
    SELECT COUNT(*) as count FROM doubts d
    JOIN courses c ON d.course_id = c.id
    WHERE c.tutor_id = ? AND d.status = 'open'
  `).get(req.user.id) as any;
  const pendingReviews = db.prepare(`
    SELECT COUNT(*) as count FROM submissions s
    JOIN problems p ON s.problem_id = p.id
    WHERE p.tutor_id = ? AND s.feedback IS NULL
  `).get(req.user.id) as any;

  const recentActivity = db.prepare(`
    SELECT u.name as studentName, u.profile_image as studentImage, p.title as problemTitle, s.status, s.created_at
    FROM submissions s
    JOIN users u ON s.user_id = u.id
    JOIN problems p ON s.problem_id = p.id
    WHERE p.tutor_id = ?
    ORDER BY s.created_at DESC LIMIT 5
  `).all(req.user.id);

  res.json({
    metrics: {
      totalCourses: totalCourses.count,
      totalLearners: totalEnrolled.count,
      activeDoubts: activeDoubts.count,
      pendingReviews: pendingReviews.count,
      revenue: 0 // Placeholder for now
    },
    recentActivity
  });
});

app.post("/api/courses", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { title, description, difficulty, tags, thumbnail, price, status } = req.body;
  const result = db.prepare(`
    INSERT INTO courses (tutor_id, title, description, difficulty, tags, thumbnail, price, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `).run(req.user.id, title, description, difficulty, tags, thumbnail, price, status || 'draft');
  res.json({ id: result.lastInsertRowid });
});

app.get("/api/tutor/courses", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const courses = db.prepare("SELECT * FROM courses WHERE tutor_id = ?").all(req.user.id);
  res.json(courses);
});

app.get("/api/courses", authenticate, (req, res) => {
  const courses = db.prepare("SELECT c.*, u.name as tutor_name FROM courses c JOIN users u ON c.tutor_id = u.id WHERE c.status = 'published'").all();
  res.json(courses);
});

app.get("/api/courses/:id", authenticate, (req, res) => {
  const course = db.prepare("SELECT c.*, u.name as tutor_name FROM courses c JOIN users u ON c.tutor_id = u.id WHERE c.id = ?").get(req.params.id) as any;
  if (!course) return res.status(404).json({ error: "Course not found" });
  const modules = db.prepare("SELECT * FROM course_modules WHERE course_id = ? ORDER BY order_index").all(req.params.id);
  res.json({ ...course, modules });
});

app.put("/api/courses/:id", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { title, description, difficulty, tags, thumbnail, price, status } = req.body;
  db.prepare(`
    UPDATE courses SET title = ?, description = ?, difficulty = ?, tags = ?, thumbnail = ?, price = ?, status = ?
    WHERE id = ? AND tutor_id = ?
  `).run(title, description, difficulty, tags, thumbnail, price, status, req.params.id, req.user.id);
  res.json({ success: true });
});

app.delete("/api/courses/:id", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  db.prepare("DELETE FROM courses WHERE id = ? AND tutor_id = ?").run(req.params.id, req.user.id);
  res.json({ success: true });
});

app.post("/api/courses/:id/modules", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { title, video_url, notes, order_index } = req.body;
  const result = db.prepare(`
    INSERT INTO course_modules (course_id, title, video_url, notes, order_index)
    VALUES (?, ?, ?, ?, ?)
  `).run(req.params.id, title, video_url, notes, order_index);
  res.json({ id: result.lastInsertRowid });
});

app.get("/api/courses/:id/modules", authenticate, (req, res) => {
  const modules = db.prepare("SELECT * FROM course_modules WHERE course_id = ? ORDER BY order_index").all(req.params.id);
  res.json(modules);
});

app.post("/api/tutor/problems", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { id, title, difficulty, topic_tags, description, constraints, edge_cases, samples, tests, mastery_impact, estimated_time, module_id } = req.body;
  db.prepare(`
    INSERT INTO problems (id, title, difficulty, topic_tags, description, constraints, edge_cases, samples, tests, mastery_impact, estimated_time, module_id, tutor_id)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(id, title, difficulty, topic_tags, description, constraints, edge_cases, samples, tests, mastery_impact, estimated_time, module_id, req.user.id);
  res.json({ success: true });
});

app.get("/api/tutor/submissions", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const submissions = db.prepare(`
    SELECT s.*, u.name as studentName, p.title as problemTitle
    FROM submissions s
    JOIN users u ON s.user_id = u.id
    JOIN problems p ON s.problem_id = p.id
    WHERE p.tutor_id = ?
    ORDER BY s.created_at DESC
  `).all(req.user.id);
  res.json(submissions);
});

app.post("/api/submissions/:id/review", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { feedback } = req.body;
  db.prepare("UPDATE submissions SET feedback = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?").run(feedback, req.params.id);
  res.json({ success: true });
});

app.get("/api/tutor/doubts", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const doubts = db.prepare(`
    SELECT d.*, u.name as studentName, c.title as courseTitle, p.title as problemTitle
    FROM doubts d
    JOIN users u ON d.user_id = u.id
    JOIN courses c ON d.course_id = c.id
    LEFT JOIN problems p ON d.problem_id = p.id
    WHERE c.tutor_id = ?
    ORDER BY d.created_at DESC
  `).all(req.user.id);
  res.json(doubts);
});

app.post("/api/doubts/:id/reply", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { answer } = req.body;
  db.prepare("UPDATE doubts SET answer = ?, status = 'resolved' WHERE id = ?").run(answer, req.params.id);
  res.json({ success: true });
});

app.get("/api/tutor/analytics", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { range } = req.query;
  
  const coursePerformance = db.prepare(`
    SELECT c.title, COUNT(e.user_id) as enrollments
    FROM courses c
    LEFT JOIN enrollments e ON c.id = e.course_id
    WHERE c.tutor_id = ?
    GROUP BY c.id
  `).all(req.user.id);

  const problemStats = db.prepare(`
    SELECT p.title, COUNT(s.id) as submissions, AVG(s.score) as avgScore
    FROM problems p
    LEFT JOIN submissions s ON p.id = s.problem_id
    WHERE p.tutor_id = ?
    GROUP BY p.id
  `).all(req.user.id);

  res.json({ coursePerformance, problemStats });
});

app.get("/api/tutor/export-report", authenticate, (req: any, res) => {
  if (req.user.role !== 'Tutor') return res.status(403).json({ error: "Forbidden" });
  const { range } = req.query;
  
  const coursePerformance = db.prepare(`
    SELECT c.title, COUNT(e.user_id) as enrollments
    FROM courses c
    LEFT JOIN enrollments e ON c.id = e.course_id
    WHERE c.tutor_id = ?
    GROUP BY c.id
  `).all(req.user.id) as any[];

  const problemStats = db.prepare(`
    SELECT p.title, COUNT(s.id) as submissions, AVG(s.score) as avgScore
    FROM problems p
    LEFT JOIN submissions s ON p.id = s.problem_id
    WHERE p.tutor_id = ?
    GROUP BY p.id
  `).all(req.user.id) as any[];

  let csv = `SkillForge Tutor Performance Report\n`;
  csv += `Tutor: ${req.user.email}\n`;
  csv += `Date: ${new Date().toLocaleDateString()}\n`;
  csv += `Time Range: ${range || 'All Time'}\n\n`;
  
  csv += `Course Performance\n`;
  csv += `Course Title,Enrollments\n`;
  coursePerformance.forEach(c => {
    csv += `"${c.title}",${c.enrollments}\n`;
  });
  
  csv += `\nProblem Statistics\n`;
  csv += `Problem Title,Submissions,Avg Score\n`;
  problemStats.forEach(p => {
    csv += `"${p.title}",${p.submissions},${p.avgScore ? Math.round(p.avgScore) : 0}%\n`;
  });

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', `attachment; filename=skillforge-tutor-report-${Date.now()}.csv`);
  res.status(200).send(csv);
});

app.post("/api/courses/:id/enroll", authenticate, (req: any, res) => {
  try {
    db.prepare("INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)").run(req.user.id, req.params.id);
    res.json({ success: true });
  } catch (err) {
    res.status(400).json({ error: "Already enrolled" });
  }
});

app.get("/api/user/enrollments", authenticate, (req: any, res) => {
  const enrollments = db.prepare(`
    SELECT c.*, u.name as tutor_name
    FROM enrollments e
    JOIN courses c ON e.course_id = c.id
    JOIN users u ON c.tutor_id = u.id
    WHERE e.user_id = ?
  `).all(req.user.id);
  res.json(enrollments);
});

app.post("/api/doubts", authenticate, (req: any, res) => {
  const { courseId, problemId, question } = req.body;
  db.prepare("INSERT INTO doubts (user_id, course_id, problem_id, question) VALUES (?, ?, ?, ?)")
    .run(req.user.id, courseId, problemId, question);
  res.json({ success: true });
});

app.get("/api/user/doubts", authenticate, (req: any, res) => {
  const doubts = db.prepare(`
    SELECT d.*, c.title as courseTitle, p.title as problemTitle
    FROM doubts d
    JOIN courses c ON d.course_id = c.id
    LEFT JOIN problems p ON d.problem_id = p.id
    WHERE d.user_id = ?
    ORDER BY d.created_at DESC
  `).all(req.user.id);
  res.json(doubts);
});

// Vite middleware for development
if (process.env.NODE_ENV !== "production") {
  const vite = await createViteServer({
    server: { middlewareMode: true },
    appType: "spa",
  });
  app.use(vite.middlewares);
} else {
  app.use(express.static(path.join(__dirname, "dist")));
  app.get("*", (req, res) => {
    res.sendFile(path.join(__dirname, "dist", "index.html"));
  });
}

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
