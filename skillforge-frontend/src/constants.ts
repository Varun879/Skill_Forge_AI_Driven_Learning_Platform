export const MOCK_PROBLEMS = [
  {
    id: '1',
    title: 'Maximum Subarray Sum',
    difficulty: 'Medium',
    topics: ['Arrays', 'Dynamic Programming'],
    estimatedTime: '25 mins',
    masteryImpact: 12,
    description: `Given an integer array \`nums\`, find the subarray with the largest sum and return its sum.
    
    A **subarray** is a contiguous non-empty sequence of elements within an array.
    
    ### Real-world Context
    This problem is often used in financial analysis to find the period with the maximum profit in a series of stock price changes.`,
    inputFormat: 'An array of integers \`nums\`.',
    outputFormat: 'A single integer representing the maximum sum.',
    constraints: [
      '1 ≤ nums.length ≤ 10^5',
      '-10^4 ≤ nums[i] ≤ 10^4',
      'Time complexity target: O(n)'
    ],
    sampleTestCases: [
      {
        input: '[-2, 1, -3, 4, -1, 2, 1, -5, 4]',
        output: '6',
        explanation: 'The subarray [4, -1, 2, 1] has the largest sum 6.'
      },
      {
        input: '[1]',
        output: '1',
        explanation: 'The subarray [1] has the largest sum 1.'
      }
    ],
    edgeCases: [
      'Empty array (should handle length 1 as minimum)',
      'All negative numbers',
      'Large input with 10^5 elements',
      'Duplicate values'
    ],
    initialCode: {
      javascript: 'function maxSubArray(nums) {\n  // Write your code here\n}',
      python: 'def maxSubArray(nums):\n    # Write your code here\n    pass',
      cpp: 'int maxSubArray(vector<int>& nums) {\n    // Write your code here\n}'
    }
  },
  {
    id: '2',
    title: 'Valid Parentheses',
    difficulty: 'Easy',
    topics: ['Stacks', 'Strings'],
    estimatedTime: '15 mins',
    masteryImpact: 5,
    description: 'Given a string \`s\` containing just the characters \`(\`, \`)\`, \`{\`, \`}\`, \`[\` and \`]\`, determine if the input string is valid.',
    inputFormat: 'A string \`s\`.',
    outputFormat: 'Boolean value.',
    constraints: [
      '1 ≤ s.length ≤ 10^4',
      's consists of parentheses only \`()[]{}\`'
    ],
    sampleTestCases: [
      {
        input: '"()"',
        output: 'true',
        explanation: 'Simple matching pair.'
      }
    ],
    edgeCases: [
      'Single character string',
      'Only opening brackets',
      'Mismatched types',
      'Correct order but nested incorrectly'
    ],
    initialCode: {
      javascript: 'function isValid(s) {\n  // Write your code here\n}',
      python: 'def isValid(s):\n    # Write your code here\n    pass',
      cpp: 'bool isValid(string s) {\n    // Write your code here\n}'
    }
  }
];

export const MOCK_ROADMAP = [
  {
    id: '1',
    title: 'Data Structures Fundamentals',
    completion: 85,
    locked: false,
    subtopics: [
      {
        id: '1-1',
        title: 'Arrays & Hashing',
        problems: ['1', '2'],
        completed: true
      },
      {
        id: '1-2',
        title: 'Linked Lists',
        problems: ['3', '4'],
        completed: false
      }
    ]
  },
  {
    id: '2',
    title: 'Advanced Algorithms',
    completion: 20,
    locked: false,
    subtopics: [
      {
        id: '2-1',
        title: 'Dynamic Programming',
        problems: ['5', '6'],
        completed: false
      },
      {
        id: '2-2',
        title: 'Graph Theory',
        problems: ['7', '8'],
        completed: false
      }
    ]
  },
  {
    id: '3',
    title: 'System Design',
    completion: 0,
    locked: true,
    subtopics: []
  }
];

export const MOCK_STATS = {
  masteryProgress: [
    { date: 'Mon', score: 45 },
    { date: 'Tue', score: 52 },
    { date: 'Wed', score: 48 },
    { date: 'Thu', score: 61 },
    { date: 'Fri', score: 65 },
    { date: 'Sat', score: 72 },
    { date: 'Sun', score: 78 },
  ],
  weeklyActivity: [
    { day: 'Mon', count: 4 },
    { day: 'Tue', count: 7 },
    { day: 'Wed', count: 3 },
    { day: 'Thu', count: 8 },
    { day: 'Fri', count: 5 },
    { day: 'Sat', count: 2 },
    { day: 'Sun', count: 6 },
  ],
  topicDistribution: [
    { name: 'Arrays', value: 40 },
    { name: 'Graphs', value: 25 },
    { name: 'DP', value: 20 },
    { name: 'Strings', value: 15 },
  ],
  recentSubmissions: [
    { id: 's1', problem: 'Two Sum', status: 'Accepted', time: '2 mins ago', language: 'Python' },
    { id: 's2', problem: 'Reverse Linked List', status: 'Accepted', time: '1 hour ago', language: 'JS' },
    { id: 's3', problem: 'Merge K Sorted Lists', status: 'Wrong Answer', time: '3 hours ago', language: 'C++' },
  ]
};
