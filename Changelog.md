# Changelog

## [Unreleased]

### Added
- Implemented randomization of answer order on quiz buttons
- Implemented prevention of sign renaming within the classroom
- Implemented question rotation system to prevent repetition
- Added support for multiple languages (Russian, Hebrew, and English) in questions.yml
- Implemented a system to slightly favor questions that haven't been asked in a long time
- Added configuration option to control how many nights should pass before questions can be repeated
- Expanded question set with more varied topics and difficulty levels

### Changed
- Refactored QuizManager to improve question selection and management
- Updated question loading process to support multiple languages from questions.yml
- Modified DayNightManager to handle more precise time announcements
- Improved error handling and logging throughout the plugin
- Updated Question class to handle randomized answer order

### Improved
- Enhanced quiz experience with better question variety and language support
- Optimized question management to ensure all questions are used before repeating
- Improved plugin stability and error resilience
- Enhanced quiz randomization to prevent answer pattern recognition

### Fixed
- Addressed issues with question repetition and randomization
- Improved cleanup of classroom contents on plugin disable
- Fixed issues with sign text not updating properly for different languages

## [1.0.1] - 2024-08-10

### Added
- Implemented PvP damage prevention within the classroom
- Added configuration option to control PvP permission in the classroom
- Implemented cooldown for PvP warning messages
- Improved PvP warning message
- Improved day-night cycle with more precise countdown
- Second-by-second countdown for the last 10 seconds of each day/night phase
- Automatic classroom creation on plugin enable
- Player teleportation system for joining and respawning during day/night cycles
- Configurable day location in config.yml
- Improved quiz system with button state management
- Implemented button disappearance and reappearance after answering
- Added cooldown periods after answering (1 second for correct, 3 seconds for incorrect)
- Implemented question rotation to prevent repetition

### Changed
- Updated EventListener to handle PvP events in the classroom
- Modified Config class to include PvP permission setting
- Modified `DayNightManager` to handle more precise time announcements
- Updated `McTestAi1` main class to handle automatic setup on plugin enable
- Refactored timer logic in `DayNightManager` for better performance and accuracy
- Improved classroom cleanup process
- Enhanced quiz system with better hologram management
- Optimized classroom cleanup to occur only once at game start, improving performance
- Refactored `QuizManager` for better button and sign management
- Updated `ClassroomManager` to improve button and sign placement
- Modified `ButtonClickListener` to work with the new button state system

### Improved
- Enhanced player safety within the classroom environment
- Enhanced the quiz experience with better visual feedback
- Optimized question management to ensure all questions are used before repeating

### Removed
- Manual `/createroom` command functionality

### Fixed
- Addressed issues with lingering holograms and quiz elements
- Improved cleanup of classroom contents on plugin disable
- Corrected the placement of quiz buttons and signs in the classroom
- Fixed issues with sign text not updating properly
- Resolved problems with buttons floating or being misplaced

## [0.2.0] - 2023-08-08

### Added
- Main plugin class `McTestAi1` with Guice dependency injection
- Refactored project structure with separate managers and config
- Classroom generation with specified dimensions (configurable via `config.yml`)
- Protection of classroom blocks from destruction
- Floor creation with alternating glass and oak plank blocks
- Addition of wall-mounted torches for lighting
- Creation of a blackboard using black concrete
- Hologram system using ArmorStands for quiz questions
- Addition of buttons for quiz answers (A, B, C, D)
- Handling of quiz button clicks
- Basic hologram text update system
- Config system with `config.yml` support
- Event listener for protecting classroom blocks
- Day-Night cycle implementation
- Player teleportation system (to classroom at night, back to original location at day)
- Quiz system with hardcoded questions
- Score tracking for correct answers
- Dynamic day duration based on quiz performance

### Changed
- Upgraded to Java 21
- Updated Guice to version 7.0.0 for Java 21 compatibility
- Modified `DayNightManager` to handle more precise time announcements
- Updated `McTestAi1` main class to handle automatic setup on plugin enable
- Refactored timer logic in `DayNightManager` for better performance and accuracy
- Added time remaining announcements with proper singular/plural forms for seconds

### Fixed
- Classroom protection now working correctly