-- =============================================================================
-- DEADLINEGUARD AI - SEED DATA SCRIPT (DEVELOPMENT & TEST DATASET ONLY)
-- Target Engine: MySQL 8.0+
-- Storage Engine: InnoDB
-- Character Encoding: UTF8MB4
-- NOTICE:
--   The student accounts, tasks, and scores in this script are strictly for
--   development, local testing, and demonstration purposes.
--   Production users, subjects, and tasks will be created via the REST API.
--   Production priority scores are ALWAYS calculated dynamically by the
--   PriorityCalculationService whenever task data changes.
-- =============================================================================

START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE productivity_stats;
TRUNCATE TABLE ai_recommendations;
TRUNCATE TABLE notifications;
TRUNCATE TABLE study_sessions;
TRUNCATE TABLE task_priorities;
TRUNCATE TABLE tasks;
TRUNCATE TABLE subjects;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1. SEED: users (Sample Student Profiles)
-- All sample accounts use a valid BCrypt hashed password (password: "Password123!")
-- BCrypt Hash: $2a$10$7qYkPqE7.n0x8VvUuGg/kOdg6.9JbW2fE8C1g1Y6H5Z3m0R2q8Wye
-- =============================================================================
INSERT INTO users (id, name, email, password_hash, department, semester, college, created_at, updated_at) VALUES
(1, 'Alex Johnson', 'alex.johnson@nit.edu', '$2a$10$7qYkPqE7.n0x8VvUuGg/kOdg6.9JbW2fE8C1g1Y6H5Z3m0R2q8Wye', 'Computer Science & Engineering', 6, 'National Institute of Technology', '2026-08-15 09:00:00', '2026-09-20 14:30:00'),
(2, 'Priya Sharma', 'priya.sharma@iit.ac.in', '$2a$10$7qYkPqE7.n0x8VvUuGg/kOdg6.9JbW2fE8C1g1Y6H5Z3m0R2q8Wye', 'Data Science & Artificial Intelligence', 2, 'Indian Institute of Technology', '2026-08-20 11:15:00', '2026-09-21 16:45:00'),
(3, 'Marcus Vance', 'marcus.v@state.edu', '$2a$10$7qYkPqE7.n0x8VvUuGg/kOdg6.9JbW2fE8C1g1Y6H5Z3m0R2q8Wye', 'Software Engineering', 4, 'State University of Technology', '2026-09-01 08:30:00', '2026-09-22 10:00:00');

-- =============================================================================
-- 2. SEED: subjects (Per-Student Course Catalog)
-- Course codes are unique per user (uq_user_subject_code).
-- =============================================================================
INSERT INTO subjects (id, user_id, subject_name, subject_code, credits, color_hex, created_at, updated_at) VALUES
-- Alex (User 1 - B.Tech CS Semester 6)
(1, 1, 'Operating Systems', 'CS602', 4, '#2563EB', '2026-08-15 09:30:00', '2026-08-15 09:30:00'),
(2, 1, 'Computer Networks', 'CS604', 4, '#7C3AED', '2026-08-15 09:32:00', '2026-08-15 09:32:00'),
(3, 1, 'Database Management Systems', 'CS606', 3, '#059669', '2026-08-15 09:35:00', '2026-08-15 09:35:00'),
(4, 1, 'Web Application Architecture', 'CS608', 3, '#D97706', '2026-08-15 09:37:00', '2026-08-15 09:37:00'),

-- Priya (User 2 - M.Tech AI Semester 2)
(5, 2, 'Advanced Machine Learning', 'DS801', 4, '#DC2626', '2026-08-20 11:30:00', '2026-08-20 11:30:00'),
(6, 2, 'Deep Neural Architectures', 'DS802', 4, '#4F46E5', '2026-08-20 11:35:00', '2026-08-20 11:35:00'),
(7, 2, 'Distributed Big Data Systems', 'DS803', 3, '#0891B2', '2026-08-20 11:40:00', '2026-08-20 11:40:00'),

-- Marcus (User 3 - B.S. Software Engineering Semester 4)
(8, 3, 'Software Architecture & Patterns', 'SE401', 4, '#2563EB', '2026-09-01 08:45:00', '2026-09-01 08:45:00'),
(9, 3, 'Agile Project Management', 'SE403', 3, '#16A34A', '2026-09-01 08:50:00', '2026-09-01 08:50:00');

-- =============================================================================
-- 3. SEED: tasks (Sample Academic Deliverables)
-- Enforces:
--   1. completed_at IS NOT NULL for COMPLETED tasks (chk_tasks_completion).
--   2. completed_at IS NULL for non-completed tasks (chk_tasks_completion).
--   3. Task 108 has subject_id = NULL to verify non-course tasks.
-- =============================================================================
INSERT INTO tasks (id, user_id, subject_id, title, description, task_type, deadline, difficulty, academic_weight, estimated_hours, status, completed_at, created_at, updated_at) VALUES
-- Alex (User 1)
(101, 1, 1, 'Operating Systems Assignment 3', 'Implement virtual memory simulation with LRU, FIFO, and Optimal Page Replacement algorithms in C++.', 'ASSIGNMENT', '2026-09-28 23:59:00', 5, 20.00, 3.5, 'PENDING', NULL, '2026-09-20 10:00:00', '2026-09-20 10:00:00'),
(102, 1, 2, 'Computer Networks Socket Programming Lab', 'Build a concurrent multi-client chat room using BSD sockets and select/poll in Java.', 'LAB', '2026-09-30 18:00:00', 4, 15.00, 4.0, 'IN_PROGRESS', NULL, '2026-09-21 14:00:00', '2026-09-24 16:30:00'),
(103, 1, 3, 'DBMS Midterm Exam Revision', 'Thorough revision of B+ Trees indexing, Query Optimization, 2-Phase Locking, and Serializability.', 'EXAM', '2026-10-02 09:00:00', 4, 30.00, 8.0, 'PENDING', NULL, '2026-09-22 09:00:00', '2026-09-22 09:00:00'),
(104, 1, 4, 'Web Architecture Milestone 2 Project', 'Design Spring Boot REST backend with JWT and JPA entities connecting to MySQL.', 'PROJECT', '2026-10-05 23:59:00', 3, 25.00, 6.0, 'PENDING', NULL, '2026-09-23 11:30:00', '2026-09-23 11:30:00'),
(105, 1, 1, 'OS Lab Record Submission', 'Write up and format experiment records for CPU Scheduling algorithms and deadlocks.', 'RECORD', '2026-10-08 17:00:00', 2, 5.00, 1.5, 'PENDING', NULL, '2026-09-24 15:00:00', '2026-09-24 15:00:00'),
(106, 1, 3, 'DBMS Assignment 1 - Relational Algebra', 'Solve 15 complex relational algebra queries and tuple relational calculus proofs.', 'ASSIGNMENT', '2026-09-23 23:59:00', 3, 10.00, 2.5, 'COMPLETED', '2026-09-23 21:15:00', '2026-09-18 10:00:00', '2026-09-23 21:15:00'),
(107, 1, 2, 'Computer Networks Wireshark Packet Analysis', 'Analyze TCP 3-way handshake and HTTP/2 headers using Wireshark pcap traces.', 'ASSIGNMENT', '2026-09-25 17:00:00', 3, 10.00, 2.0, 'COMPLETED', '2026-09-19 12:00:00', '2026-09-25 15:40:00'),
(108, 1, NULL, 'Graduate Scholarship Application Essay', 'Draft personal statement and research interest essay for Summer Research Fellowship.', 'OTHER', '2026-10-15 23:59:00', 3, 0.00, 4.0, 'PENDING', NULL, '2026-09-24 18:00:00', '2026-09-24 18:00:00'),

-- Priya (User 2)
(201, 2, 5, 'ML Kaggle Competition Classifier', 'Fine-tune XGBoost and Random Forest on tabular clinical dataset with 10-fold cross validation.', 'PROJECT', '2026-09-29 23:59:00', 5, 25.00, 6.0, 'PENDING', NULL, '2026-09-21 10:00:00', '2026-09-21 10:00:00'),
(202, 2, 6, 'Vision Transformer Paper Presentation', 'Prepare 20-slide presentation explaining Multi-Head Self-Attention and patch embeddings in ViT.', 'PRESENTATION', '2026-10-01 10:00:00', 4, 15.00, 4.0, 'PENDING', NULL, '2026-09-22 15:00:00', '2026-09-22 15:00:00'),
(203, 2, 7, 'Spark Streaming Pipeline Lab', 'Configure Apache Spark Structured Streaming with Kafka consumer and windowed aggregates.', 'LAB', '2026-10-04 18:00:00', 4, 20.00, 5.0, 'PENDING', NULL, '2026-09-23 09:30:00', '2026-09-23 09:30:00'),

-- Marcus (User 3)
(301, 3, 8, 'Microservices Architecture Case Study', 'Write architectural decision records (ADRs) comparing event-driven and REST architectures.', 'ASSIGNMENT', '2026-09-29 17:00:00', 4, 20.00, 4.5, 'PENDING', NULL, '2026-09-23 14:00:00', '2026-09-23 14:00:00'),
(302, 3, 9, 'Sprint Retrospective & Velocity Report', 'Compile team burndown charts and agile retrospective action items for Sprint 3.', 'ASSIGNMENT', '2026-10-03 23:59:00', 2, 10.00, 2.0, 'PENDING', NULL, '2026-09-24 11:00:00', '2026-09-24 11:00:00');

-- =============================================================================
-- 4. SEED: task_priorities (EXACT MATHEMATICALLY VERIFIED SCORES)
-- FORMULA:
--   priority_score = 0.40*urgency + 0.20*difficulty + 0.20*weight + 0.10*effort + 0.10*workload
-- TIERS:
--   76.00 - 100.00 = CRITICAL | 51.00 - 75.99 = HIGH | 31.00 - 50.99 = MEDIUM | 0.00 - 30.99 = LOW
--
-- DETAILED CALCULATIONS:
--   Task 101: 0.40(92.50) + 0.20(100.00) + 0.20(75.00) + 0.10(70.00) + 0.10(80.00) = 37.00 + 20.00 + 15.00 + 7.00 + 8.00 = 87.00 -> CRITICAL
--   Task 102: 0.40(60.00) + 0.20(75.00)  + 0.20(50.00) + 0.10(40.00) + 0.10(60.00) = 24.00 + 15.00 + 10.00 + 4.00 + 6.00 = 59.00 -> HIGH
--   Task 103: 0.40(55.00) + 0.20(75.00)  + 0.20(95.00) + 0.10(80.00) + 0.10(50.00) = 22.00 + 15.00 + 19.00 + 8.00 + 5.00 = 69.00 -> HIGH
--   Task 104: 0.40(35.00) + 0.20(50.00)  + 0.20(62.50) + 0.10(60.00) + 0.10(40.00) = 14.00 + 10.00 + 12.50 + 6.00 + 4.00 = 46.50 -> MEDIUM
--   Task 105: 0.40(18.00) + 0.20(25.00)  + 0.20(15.00) + 0.10(15.00) + 0.10(20.00) =  7.20 +  5.00 +  3.00 + 1.50 + 2.00 = 18.70 -> LOW
--   Task 108: 0.40(12.00) + 0.20(50.00)  + 0.20(0.00)  + 0.10(40.00) + 0.10(20.00) =  4.80 + 10.00 +  0.00 + 4.00 + 2.00 = 20.80 -> LOW
--   Task 201: 0.40(88.00) + 0.20(100.00) + 0.20(75.00) + 0.10(70.00) + 0.10(60.00) = 35.20 + 20.00 + 15.00 + 7.00 + 6.00 = 83.20 -> CRITICAL
--   Task 202: 0.40(65.00) + 0.20(75.00)  + 0.20(50.00) + 0.10(40.00) + 0.10(40.00) = 26.00 + 15.00 + 10.00 + 4.00 + 4.00 = 59.00 -> HIGH
--   Task 203: 0.40(40.00) + 0.20(75.00)  + 0.20(60.00) + 0.10(50.00) + 0.10(40.00) = 16.00 + 15.00 + 12.00 + 5.00 + 4.00 = 52.00 -> HIGH
--   Task 301: 0.40(82.00) + 0.20(75.00)  + 0.20(65.00) + 0.10(60.00) + 0.10(50.00) = 32.80 + 15.00 + 13.00 + 6.00 + 5.00 = 71.80 -> HIGH
--   Task 302: 0.40(38.00) + 0.20(25.00)  + 0.20(30.00) + 0.10(20.00) + 0.10(30.00) = 15.20 +  5.00 +  6.00 + 2.00 + 3.00 = 31.20 -> MEDIUM
-- =============================================================================
INSERT INTO task_priorities (id, task_id, priority_score, priority_level, urgency_score, difficulty_score, weight_score, effort_score, workload_score, explanation_text, calculated_at) VALUES
(1, 101, 87.00, 'CRITICAL', 92.50, 100.00, 75.00, 70.00, 80.00, 'CRITICAL priority: Task is due in less than 30 hours, has maximum difficulty (5/5), and carries 20% of course grade with 3 other upcoming deadlines.', '2026-09-26 08:00:00'),
(2, 102, 59.00, 'HIGH', 60.00, 75.00, 50.00, 40.00, 60.00, 'HIGH priority: Laboratory deliverable due in 4 days requiring hands-on socket debugging and concurrent threading.', '2026-09-26 08:00:00'),
(3, 103, 69.00, 'HIGH', 55.00, 75.00, 95.00, 80.00, 50.00, 'HIGH priority: Significant academic weight (30% of total grade) with extensive 8-hour estimated preparation requirement.', '2026-09-26 08:00:00'),
(4, 104, 46.50, 'MEDIUM', 35.00, 50.00, 62.50, 60.00, 40.00, 'MEDIUM priority: Healthy runway of 9 days. Moderate complexity but substantial semester weight.', '2026-09-26 08:00:00'),
(5, 105, 18.70, 'LOW', 18.00, 25.00, 15.00, 15.00, 20.00, 'LOW priority: Due in 12 days, low difficulty rating (2/5), and minor academic grade weight (5%).', '2026-09-26 08:00:00'),
(6, 108, 20.80, 'LOW', 12.00, 50.00, 0.00, 40.00, 20.00, 'LOW priority: Scholarship essay due in 19 days. Does not affect course GPA but requires writing effort.', '2026-09-26 08:00:00'),
(7, 201, 83.20, 'CRITICAL', 88.00, 100.00, 75.00, 70.00, 60.00, 'CRITICAL priority: Machine learning project deadline in 3 days, maximum algorithmic difficulty (5/5), and 25% course weight.', '2026-09-26 08:00:00'),
(8, 202, 59.00, 'HIGH', 65.00, 75.00, 50.00, 40.00, 40.00, 'HIGH priority: Technical research presentation due in 5 days requiring slide preparation and literature review.', '2026-09-26 08:00:00'),
(9, 203, 52.00, 'HIGH', 40.00, 75.00, 60.00, 50.00, 40.00, 'HIGH priority: Distributed streaming lab involving multi-node Kafka configuration and Spark queries.', '2026-09-26 08:00:00'),
(10, 301, 71.80, 'HIGH', 82.00, 75.00, 65.00, 60.00, 50.00, 'HIGH priority: Software architecture case study due in 3 days with rigorous ADR documentation criteria.', '2026-09-26 08:00:00'),
(11, 302, 31.20, 'MEDIUM', 38.00, 25.00, 30.00, 20.00, 30.00, 'MEDIUM priority: Routine sprint retrospective compilation with moderate difficulty.', '2026-09-26 08:00:00');

-- =============================================================================
-- 5. SEED: study_sessions (Sample Study Tracker Records)
-- Supports both task-associated sessions and general focus sessions (task_id = NULL)
-- =============================================================================
INSERT INTO study_sessions (id, user_id, task_id, start_time, end_time, duration_minutes, is_completed, notes, created_at) VALUES
(1, 1, 101, '2026-09-25 18:00:00', '2026-09-25 19:30:00', 90, TRUE, 'Implemented FIFO and basic page fault metrics. Remaining: LRU algorithm.', '2026-09-25 19:30:00'),
(2, 1, 102, '2026-09-25 20:00:00', '2026-09-25 20:50:00', 50, TRUE, 'Completed socket initialization and client connect routines.', '2026-09-25 20:50:00'),
(3, 1, 103, '2026-09-26 07:00:00', '2026-09-26 08:15:00', 75, TRUE, 'Reviewed B+ Tree leaf node splitting and internal index structures.', '2026-09-26 08:15:00'),
(4, 1, NULL, '2026-09-26 08:30:00', '2026-09-26 09:15:00', 45, TRUE, 'General technical reading on distributed consensus algorithms.', '2026-09-26 09:15:00'),
(5, 2, 201, '2026-09-25 14:00:00', '2026-09-25 16:00:00', 120, TRUE, 'Completed exploratory data analysis and feature correlation matrix.', '2026-09-25 16:00:00'),
(6, 3, 301, '2026-09-25 19:00:00', '2026-09-25 20:30:00', 90, TRUE, 'Drafted ADR-001 comparing Kafka vs RabbitMQ message brokers.', '2026-09-25 20:30:00');

-- =============================================================================
-- 6. SEED: notifications (Sample Urgency Reminders)
-- =============================================================================
INSERT INTO notifications (id, user_id, task_id, title, message, notification_type, is_read, created_at) VALUES
(1, 1, 101, 'Urgent: OS Assignment Due Soon', 'Operating Systems Assignment 3 is due in less than 30 hours. Priority score is 87.00 (CRITICAL). Complete this before lower-priority tasks.', 'DEADLINE_24H', FALSE, '2026-09-26 07:30:00'),
(2, 1, 102, 'Reminder: Networks Lab Deliverable', 'Computer Networks Socket Programming Lab is due in 4 days. You have logged 50 mins of focus time so far.', 'DEADLINE_48H', FALSE, '2026-09-26 08:00:00'),
(3, 1, NULL, 'Daily Morning Briefing', 'Good morning Alex! You have 1 Critical task and 2 High priority deliverables pending this week. Start with Operating Systems.', 'DAILY_SUMMARY', TRUE, '2026-09-26 06:30:00'),
(4, 2, 201, 'Critical Task Alert: ML Competition', 'ML Kaggle Competition Classifier is due in 3 days. High academic weight (25%). Priority score: 83.20.', 'HIGH_PRIORITY', FALSE, '2026-09-26 07:00:00'),
(5, 3, 301, 'Deadline Alert: Software Architecture Case Study', 'Case Study deliverable due in 3 days. Priority score: 71.80.', 'DEADLINE_48H', FALSE, '2026-09-26 07:15:00');

-- =============================================================================
-- 7. SEED: ai_recommendations (Sample AI Reasoning Logs)
-- =============================================================================
INSERT INTO ai_recommendations (id, user_id, task_id, recommendation_type, recommendation_text, ai_reasoning, suggested_action, generated_at) VALUES
(1, 1, 101, 'WHAT_SHOULD_I_DO_NOW', 
 'Focus immediately on Operating Systems Assignment 3 (Virtual Memory Simulation).', 
 'This deliverable has your highest calculated priority score (87.00/100). The deadline is in 28 hours, it carries 20% of your course grade, and requires high cognitive focus due to complex page replacement pointer arithmetic.', 
 'Dedicate a 90-minute uninterrupted study block to finish the LRU page replacement algorithm before working on test benchmarks.', 
 '2026-09-26 08:05:00'),

(2, 1, NULL, 'STUDY_PLAN', 
 'Today''s 4-Hour Academic Acceleration Plan', 
 'Optimized schedule based on 4 available hours today, balancing urgent algorithmic coding with conceptual revision.', 
 'Block 1: 06:00 PM - 07:30 PM (OS Assignment 3) | Break: 15 mins | Block 2: 07:45 PM - 08:45 PM (Socket Programming Lab) | Break: 15 mins | Block 3: 09:00 PM - 10:00 PM (DBMS Midterm Prep).', 
 '2026-09-26 08:10:00'),

(3, 2, 201, 'WHAT_SHOULD_I_DO_NOW', 
 'Prioritize the ML Kaggle Competition Classifier pipeline.', 
 'Accounts for 25% of your semester grade with a tight 3-day turnaround and high computational experiment turnaround times.', 
 'Launch hyperparameter tuning grid searches on Google Colab or Kaggle GPUs now so models train in the background.', 
 '2026-09-26 08:15:00');

-- =============================================================================
-- 8. SEED: productivity_stats (Sample 7-Day Performance Metrics)
-- =============================================================================
INSERT INTO productivity_stats (id, user_id, stat_date, tasks_completed, tasks_overdue, study_minutes, productivity_score, created_at) VALUES
-- Alex (User 1) Past 7 Days
(1, 1, '2026-09-20', 1, 0, 110, 85.00, '2026-09-20 23:59:00'),
(2, 1, '2026-09-21', 0, 0, 60, 65.00, '2026-09-21 23:59:00'),
(3, 1, '2026-09-22', 1, 0, 135, 90.00, '2026-09-22 23:59:00'),
(4, 1, '2026-09-23', 2, 0, 180, 95.00, '2026-09-23 23:59:00'),
(5, 1, '2026-09-24', 0, 0, 90, 70.00, '2026-09-24 23:59:00'),
(6, 1, '2026-09-25', 1, 0, 140, 88.00, '2026-09-25 23:59:00'),
(7, 1, '2026-09-26', 0, 0, 75, 78.00, '2026-09-26 12:00:00'),

-- Priya (User 2)
(8, 2, '2026-09-24', 1, 0, 150, 92.00, '2026-09-24 23:59:00'),
(9, 2, '2026-09-25', 1, 0, 180, 96.00, '2026-09-25 23:59:00'),

-- Marcus (User 3)
(10, 3, '2026-09-24', 0, 0, 70, 60.00, '2026-09-24 23:59:00'),
(11, 3, '2026-09-25', 1, 0, 120, 85.00, '2026-09-25 23:59:00');

COMMIT;
