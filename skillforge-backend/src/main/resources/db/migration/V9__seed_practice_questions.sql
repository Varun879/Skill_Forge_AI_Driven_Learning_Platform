-- V9: Seed practice questions for programming and aptitude practice.

INSERT INTO practice_questions (question_type, title, prompt, difficulty_level, topic, estimated_solve_time_minutes, success_rate, is_active)
VALUES
    ('PROGRAMMING_MCQ', 'Array lookup complexity', 'What is the average time complexity of accessing an element in an array by index?', 'BEGINNER', 'Data Structures', 2, 68.00, TRUE),
    ('PROGRAMMING_MCQ', 'Java inheritance keyword', 'Which keyword is used in Java to inherit a class?', 'BEGINNER', 'Java OOP', 2, 74.00, TRUE),
    ('PROGRAMMING_MCQ', 'SQL filtering clause', 'Which SQL clause is used to filter rows before grouping?', 'INTERMEDIATE', 'SQL', 3, 57.00, TRUE),
    ('PROGRAMMING_MCQ', 'Recursion base case', 'Why is a base case necessary in recursion?', 'INTERMEDIATE', 'Algorithms', 4, 49.00, TRUE),
    ('PROGRAMMING_MCQ', 'JavaScript event loop', 'What does the JavaScript event loop primarily do?', 'ADVANCED', 'JavaScript Runtime', 5, 41.00, TRUE),
    ('PROGRAMMING_MCQ', 'Binary search prerequisite', 'What must be true before binary search can be correctly applied to a collection?', 'INTERMEDIATE', 'Searching', 3, 53.00, TRUE),
    ('APTITUDE_MCQ', 'Percentage increase', 'A value rises from 80 to 100. What is the percentage increase?', 'BEGINNER', 'Percentages', 2, 71.00, TRUE),
    ('APTITUDE_MCQ', 'Ratio simplification', 'Simplify the ratio 18:24.', 'BEGINNER', 'Ratios', 2, 78.00, TRUE),
    ('APTITUDE_MCQ', 'Time and work', 'If one worker completes a job in 6 days, what fraction of the job is done in 1 day?', 'BEGINNER', 'Time and Work', 2, 73.00, TRUE),
    ('APTITUDE_MCQ', 'Average speed', 'A car travels 150 km in 3 hours. What is its average speed?', 'BEGINNER', 'Speed Distance Time', 2, 82.00, TRUE),
    ('APTITUDE_MCQ', 'Probability of a coin toss', 'What is the probability of getting heads in a fair coin toss?', 'BEGINNER', 'Probability', 2, 86.00, TRUE),
    ('APTITUDE_MCQ', 'Number pattern', 'Find the next number in the series: 2, 6, 12, 20, 30, ?', 'INTERMEDIATE', 'Logical Reasoning', 4, 46.00, TRUE);

INSERT INTO programming_mcqs (question_id, explanation)
SELECT id,
       CASE title
           WHEN 'Array lookup complexity' THEN 'Direct index access is constant time because the address offset can be computed immediately.'
           WHEN 'Java inheritance keyword' THEN 'A Java class inherits from another class using the extends keyword.'
           WHEN 'SQL filtering clause' THEN 'WHERE filters rows before GROUP BY is applied. HAVING filters after grouping.'
           WHEN 'Recursion base case' THEN 'Without a base case, recursive calls continue until stack overflow.'
           WHEN 'JavaScript event loop' THEN 'The event loop coordinates the call stack and task queues so async callbacks run after the stack clears.'
           WHEN 'Binary search prerequisite' THEN 'Binary search requires the data to be sorted for comparisons to eliminate half the space correctly.'
       END
FROM practice_questions
WHERE question_type = 'PROGRAMMING_MCQ';

INSERT INTO aptitude_mcqs (question_id, explanation)
SELECT id,
       CASE title
           WHEN 'Percentage increase' THEN 'Increase is 20 on a base of 80, so percentage increase is (20/80) x 100 = 25%.'
           WHEN 'Ratio simplification' THEN 'Both terms divide by 6, giving 3:4.'
           WHEN 'Time and work' THEN 'Completing the whole job in 6 days means 1/6 of the job is completed per day.'
           WHEN 'Average speed' THEN 'Average speed = distance / time = 150 / 3 = 50 km/h.'
           WHEN 'Probability of a coin toss' THEN 'There are two equally likely outcomes and one favorable outcome, so probability is 1/2.'
           WHEN 'Number pattern' THEN 'The pattern adds consecutive even differences: +4, +6, +8, +10, so next is +12 giving 42.'
       END
FROM practice_questions
WHERE question_type = 'APTITUDE_MCQ';

INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'arrays' FROM practice_questions WHERE title = 'Array lookup complexity';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'complexity' FROM practice_questions WHERE title = 'Array lookup complexity';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'basics' FROM practice_questions WHERE title = 'Array lookup complexity';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'java' FROM practice_questions WHERE title = 'Java inheritance keyword';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'oop' FROM practice_questions WHERE title = 'Java inheritance keyword';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'inheritance' FROM practice_questions WHERE title = 'Java inheritance keyword';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'sql' FROM practice_questions WHERE title = 'SQL filtering clause';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'database' FROM practice_questions WHERE title = 'SQL filtering clause';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'querying' FROM practice_questions WHERE title = 'SQL filtering clause';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'recursion' FROM practice_questions WHERE title = 'Recursion base case';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'algorithms' FROM practice_questions WHERE title = 'Recursion base case';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'functions' FROM practice_questions WHERE title = 'Recursion base case';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'javascript' FROM practice_questions WHERE title = 'JavaScript event loop';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'async' FROM practice_questions WHERE title = 'JavaScript event loop';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'event-loop' FROM practice_questions WHERE title = 'JavaScript event loop';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'binary-search' FROM practice_questions WHERE title = 'Binary search prerequisite';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'algorithms' FROM practice_questions WHERE title = 'Binary search prerequisite';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'sorting' FROM practice_questions WHERE title = 'Binary search prerequisite';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'percentages' FROM practice_questions WHERE title = 'Percentage increase';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'arithmetic' FROM practice_questions WHERE title = 'Percentage increase';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'growth' FROM practice_questions WHERE title = 'Percentage increase';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'ratios' FROM practice_questions WHERE title = 'Ratio simplification';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'simplification' FROM practice_questions WHERE title = 'Ratio simplification';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'basics' FROM practice_questions WHERE title = 'Ratio simplification';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'time-and-work' FROM practice_questions WHERE title = 'Time and work';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'fractions' FROM practice_questions WHERE title = 'Time and work';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'productivity' FROM practice_questions WHERE title = 'Time and work';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'speed' FROM practice_questions WHERE title = 'Average speed';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'distance' FROM practice_questions WHERE title = 'Average speed';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'time' FROM practice_questions WHERE title = 'Average speed';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'probability' FROM practice_questions WHERE title = 'Probability of a coin toss';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'coin-toss' FROM practice_questions WHERE title = 'Probability of a coin toss';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'basics' FROM practice_questions WHERE title = 'Probability of a coin toss';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'series' FROM practice_questions WHERE title = 'Number pattern';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'patterns' FROM practice_questions WHERE title = 'Number pattern';
INSERT INTO practice_question_tags (question_id, tag)
SELECT id, 'reasoning' FROM practice_questions WHERE title = 'Number pattern';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'O(1)', 1, TRUE FROM practice_questions WHERE title = 'Array lookup complexity';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'O(log n)', 2, FALSE FROM practice_questions WHERE title = 'Array lookup complexity';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'O(n)', 3, FALSE FROM practice_questions WHERE title = 'Array lookup complexity';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'O(n log n)', 4, FALSE FROM practice_questions WHERE title = 'Array lookup complexity';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'implements', 1, FALSE FROM practice_questions WHERE title = 'Java inheritance keyword';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'inherits', 2, FALSE FROM practice_questions WHERE title = 'Java inheritance keyword';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'extends', 3, TRUE FROM practice_questions WHERE title = 'Java inheritance keyword';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'super', 4, FALSE FROM practice_questions WHERE title = 'Java inheritance keyword';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'HAVING', 1, FALSE FROM practice_questions WHERE title = 'SQL filtering clause';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'WHERE', 2, TRUE FROM practice_questions WHERE title = 'SQL filtering clause';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'ORDER BY', 3, FALSE FROM practice_questions WHERE title = 'SQL filtering clause';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'LIMIT', 4, FALSE FROM practice_questions WHERE title = 'SQL filtering clause';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'To make code run faster in all cases', 1, FALSE FROM practice_questions WHERE title = 'Recursion base case';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'To stop recursive calls from continuing forever', 2, TRUE FROM practice_questions WHERE title = 'Recursion base case';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'To allocate heap memory', 3, FALSE FROM practice_questions WHERE title = 'Recursion base case';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'To replace loops automatically', 4, FALSE FROM practice_questions WHERE title = 'Recursion base case';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'Compiles JavaScript to bytecode', 1, FALSE FROM practice_questions WHERE title = 'JavaScript event loop';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'Schedules asynchronous callbacks when the call stack is free', 2, TRUE FROM practice_questions WHERE title = 'JavaScript event loop';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'Stores variables in closure scope', 3, FALSE FROM practice_questions WHERE title = 'JavaScript event loop';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'Creates new threads for every promise', 4, FALSE FROM practice_questions WHERE title = 'JavaScript event loop';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'The collection must be sorted', 1, TRUE FROM practice_questions WHERE title = 'Binary search prerequisite';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'The collection must contain unique values', 2, FALSE FROM practice_questions WHERE title = 'Binary search prerequisite';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'The collection must be stored in a linked list', 3, FALSE FROM practice_questions WHERE title = 'Binary search prerequisite';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, 'The collection size must be a power of two', 4, FALSE FROM practice_questions WHERE title = 'Binary search prerequisite';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '20%', 1, FALSE FROM practice_questions WHERE title = 'Percentage increase';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '25%', 2, TRUE FROM practice_questions WHERE title = 'Percentage increase';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '30%', 3, FALSE FROM practice_questions WHERE title = 'Percentage increase';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '40%', 4, FALSE FROM practice_questions WHERE title = 'Percentage increase';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '2:3', 1, FALSE FROM practice_questions WHERE title = 'Ratio simplification';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '3:4', 2, TRUE FROM practice_questions WHERE title = 'Ratio simplification';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '4:3', 3, FALSE FROM practice_questions WHERE title = 'Ratio simplification';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '6:8', 4, FALSE FROM practice_questions WHERE title = 'Ratio simplification';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '1/3', 1, FALSE FROM practice_questions WHERE title = 'Time and work';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '1/6', 2, TRUE FROM practice_questions WHERE title = 'Time and work';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '2/3', 3, FALSE FROM practice_questions WHERE title = 'Time and work';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '6', 4, FALSE FROM practice_questions WHERE title = 'Time and work';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '45 km/h', 1, FALSE FROM practice_questions WHERE title = 'Average speed';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '50 km/h', 2, TRUE FROM practice_questions WHERE title = 'Average speed';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '55 km/h', 3, FALSE FROM practice_questions WHERE title = 'Average speed';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '60 km/h', 4, FALSE FROM practice_questions WHERE title = 'Average speed';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '1/4', 1, FALSE FROM practice_questions WHERE title = 'Probability of a coin toss';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '1/3', 2, FALSE FROM practice_questions WHERE title = 'Probability of a coin toss';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '1/2', 3, TRUE FROM practice_questions WHERE title = 'Probability of a coin toss';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '2/3', 4, FALSE FROM practice_questions WHERE title = 'Probability of a coin toss';

INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '40', 1, FALSE FROM practice_questions WHERE title = 'Number pattern';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '42', 2, TRUE FROM practice_questions WHERE title = 'Number pattern';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '44', 3, FALSE FROM practice_questions WHERE title = 'Number pattern';
INSERT INTO mcq_options (question_id, option_text, display_order, is_correct)
SELECT id, '48', 4, FALSE FROM practice_questions WHERE title = 'Number pattern';