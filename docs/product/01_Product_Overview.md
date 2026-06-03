# VocabVerse - Product Overview

Version: 1.0

Status: Draft

Author: Product Team

Last Updated: 2026-06-02

---

# 1. Executive Summary

VocabVerse là một nền tảng học tiếng Anh tập trung vào việc ghi nhớ và thực hành từ vựng thông qua các phương pháp học hiện đại như Flashcard, Spaced Repetition, Shadowing và AI Roleplay.

Khác với các website từ điển hoặc flashcard truyền thống, VocabVerse cho phép người dùng nhập từ vựng dưới nhiều định dạng khác nhau, sau đó sử dụng AI để chuẩn hóa thành cấu trúc dữ liệu thống nhất phục vụ cho việc học tập, ôn luyện và theo dõi tiến độ.

Mục tiêu của sản phẩm là giúp người học:

* Thu thập từ vựng nhanh hơn
* Ghi nhớ lâu hơn
* Thực hành nhiều hơn
* Học từ ngữ cảnh thực tế
* Theo dõi tiến độ học tập một cách trực quan

---

# 2. Product Vision

Build an AI-powered English vocabulary learning platform that transforms unstructured vocabulary inputs into structured learning experiences through flashcards, spaced repetition, shadowing, and conversational practice.

---

# 3. Product Mission

Giúp người học tiếng Anh xây dựng kho từ vựng cá nhân hóa và duy trì việc ôn tập hiệu quả bằng công nghệ AI.

---

# 4. Problem Statement

Người học tiếng Anh thường gặp các vấn đề:

## 4.1 Vocabulary Collection Problem

Từ vựng được lưu rải rác:

* Notebook
* Google Docs
* PDF
* Screenshot
* ChatGPT

Không có nơi quản lý tập trung.

## 4.2 Review Problem

Sau vài ngày:

* Quên từ
* Không ôn lại
* Không có lịch học

## 4.3 Practice Problem

Biết nghĩa từ nhưng:

* Không dùng được
* Không nhớ ngữ cảnh
* Không phát âm được

## 4.4 Content Fragmentation

Học từ:

* Youtube
* TikTok
* Netflix
* Sách

Nhưng không có hệ thống liên kết các nguồn này với nhau.

---

# 5. Product Goals

## Goal 1

Cho phép người dùng xây dựng thư viện từ vựng cá nhân.

## Goal 2

Tự động chuẩn hóa dữ liệu bằng AI.

## Goal 3

Tăng khả năng ghi nhớ thông qua Spaced Repetition.

## Goal 4

Hỗ trợ luyện nghe và phát âm bằng Shadowing.

## Goal 5

Tạo môi trường thực hành giao tiếp bằng AI Roleplay.

---

# 6. Target Audience

## Primary Users

### University Students

* TOEIC
* IELTS
* English Communication

### Self-Learners

Người tự học tiếng Anh.

### Working Professionals

* IT
* Business
* Marketing
* Sales

---

# 7. Product Scope

## Included

### Vocabulary Management

* Search Vocabulary
* Create Collection
* Edit Collection
* Import Vocabulary
* Export Vocabulary

### Learning System

* Flashcards
* Quiz
* Typing Practice
* Spaced Repetition

### Shadowing

* Video-based learning
* English subtitles
* Vietnamese subtitles

### AI Roleplay

* Dynamic Scenarios
* Difficulty Levels
* Persona System
* Grammar Correction

### Progress Tracking

* Dashboard
* Statistics
* Learning Streak

---

## Excluded In V1

* Mobile Application
* Live Teacher Classes
* Multiplayer Learning
* Marketplace Monetization
* Premium Subscription

---

# 8. User Roles

## User

Can:

* Search words
* Create collections
* Import vocabulary
* Learn via flashcards
* Take quizzes
* Enable spaced repetition
* Practice roleplay
* Study shadowing content
* Export PDF
* Share collections

---

## Admin

Can:

* Manage users
* Manage collections
* Upload shadowing videos
* Manage featured vocabulary sets
* Monitor platform activity

---

# 9. Core Features

## F01 - Vocabulary Search

Search a vocabulary word and retrieve:

* Pronunciation
* Meanings
* Synonyms
* Antonyms
* Examples

---

## F02 - Vocabulary Import

Import vocabulary using:

* Plain Text
* JSON
* CSV
* Manual Entry

---

## F03 - AI Vocabulary Normalization

Convert unstructured input into standardized JSON.

Example:

Input:

abandon
ability
achieve

Output:

Structured Vocabulary Objects

---

## F04 - Collection Management

Users can:

* Create Collection
* Edit Collection
* Delete Collection
* Duplicate Collection
* Share Collection

---

## F05 - Flashcard Learning

Study words using:

* Front Side
* Back Side
* Reveal Answer

---

## F06 - Quiz System

Support:

* Multiple Choice
* Matching
* Fill in the Blank

---

## F07 - Typing Practice

User types the correct answer manually.

---

## F08 - Spaced Repetition

Users can:

* Enable Review Schedule
* Customize Intervals
* Receive Notifications

---

## F09 - Shadowing

Admin uploads video.

System automatically generates:

* English Subtitle
* Vietnamese Subtitle

Users practice by repeating sentences.

---

## F10 - AI Roleplay

Generate real-life conversations.

Examples:

* Restaurant
* Hotel
* Airport
* Shopping
* Interview

---

## F11 - Dashboard

Display:

* Total Words
* Words Learned
* Review Due Today
* Streak
* Accuracy

---

## F12 - PDF Export

Export collection as:

* Vocabulary Table
* Printable Flashcards

---

# 10. Success Metrics

## User Metrics

* Daily Active Users
* Weekly Active Users
* Monthly Active Users

## Learning Metrics

* Review Completion Rate
* Quiz Accuracy
* Flashcard Completion Rate

## Retention Metrics

* Day 1 Retention
* Day 7 Retention
* Day 30 Retention

---

# 11. Technology Direction

Frontend:

* React
* TypeScript
* TailwindCSS

Backend:

* Spring Boot

Database:

* PostgreSQL

Cache:

* Redis

Queue:

* RabbitMQ

AI:

* Groq
* Gemini

Authentication:

* JWT

Storage:

* S3 Compatible Storage

Notification:

* Email
* Browser Push Notification

---

# 12. Roadmap

Phase 1

Vocabulary Core

Phase 2

Spaced Repetition

Phase 3

Dashboard & Public Collections

Phase 4

Shadowing

Phase 5

AI Roleplay

Phase 6

Mobile Application

---

# 13. Future Expansion

* AI Pronunciation Scoring
* AI Writing Correction
* AI Speaking Coach
* Mobile App
* Premium Subscription
* Community Marketplace
* Vocabulary Recommendation Engine

---

END OF DOCUMENT
