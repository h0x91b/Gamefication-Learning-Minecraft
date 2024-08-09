# Changelog

## [Unreleased]

### Added
- Improved day-night cycle with more precise countdown
- Second-by-second countdown for the last 10 seconds of each day/night phase
- Automatic classroom creation on plugin enable
- Player teleportation system for joining and respawning during day/night cycles
- Configurable day location in config.yml

### Changed
- Modified `DayNightManager` to handle more precise time announcements
- Updated `McTestAi1` main class to handle automatic setup on plugin enable
- Refactored timer logic in `DayNightManager` for better performance and accuracy
- Improved classroom cleanup process
- Enhanced quiz system with better hologram management
- Optimized classroom cleanup to occur only once at game start, improving performance

### Removed
- Manual `/createroom` command functionality

### Fixed
- Addressed issues with lingering holograms and quiz elements
- Improved cleanup of classroom contents on plugin disable

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