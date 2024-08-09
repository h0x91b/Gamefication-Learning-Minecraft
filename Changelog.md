# CHANGELOG

## [Unreleased]

### Added
- Automatic classroom creation on plugin startup
- Immediate start of day-night cycle after plugin initialization
- Logging for automatic classroom creation and day-night cycle start

### Changed
- Removed manual `/createroom` command functionality
- Modified `DayNightManager` to start cycle immediately after classroom creation
- Updated `McTestAi1` main class to handle automatic setup on plugin enable

### Removed
- `/createroom` command and its associated logic

### Improved
- Plugin initialization process for a more streamlined startup
- Error handling and logging for classroom creation and day-night cycle start

### Added
- Main plugin class `McTestAi1` with Guice dependency injection
- Refactored project structure with separate managers and config
- `/createroom` command for creating a classroom
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

### Fixed
- Classroom protection now working correctly

### Work in Progress
- Full integration of inventory management system
- Proper cleanup of holograms and buttons when recreating the room

### TODO
- Implement sound effects for correct/incorrect answers
- Add admin rights check for the `/createroom` command
- Implement error handling when creating a room
- Add handling of player logins depending on the time of day
- Integrate with an external API for dynamic question generation
- Implement a leaderboard system
- Add visual effects for correct/incorrect answers

### Known Issues
- No admin rights check for the `/createroom` command
- No error handling when creating a room
- Holograms and buttons are not properly removed when recreating the room
- Inventory management during night/day cycle not fully implemented