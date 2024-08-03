# CHANGELOG

## [Unreleased]

### Added
- Main plugin class `McTestAi1` with Guice dependency injection
- Refactored project structure with separate managers and config
- `/createroom` command for creating a classroom
- Classroom generation with specified dimensions (15x10x5 interior)
- Protection of classroom blocks from destruction
- Floor creation with alternating glass and oak plank blocks
- Addition of wall-mounted torches for lighting
- Creation of a blackboard using black concrete
- Hologram system using ArmorStands
- Addition of buttons and signs for quiz (A, B, C, D)
- Handling of quiz button clicks
- Basic hologram text update system
- Config system with `config.yml` support
- Event listener for protecting classroom blocks

### Changed
- Upgraded to Java 21
- Updated Guice to version 7.0.0 for Java 21 compatibility

### Fixed
- Classroom protection now working correctly

### Work in Progress
- Full quiz system (questions not yet implemented)
- Day and night system (mentioned in specification, not yet implemented)
- Player teleportation to classroom and back (partially implemented)

### TODO
- Implement complete day-night cycle
- Add question and answer system for the quiz
- Implement score counting and day duration changes
- Add player inventory management (deactivation at night)
- Implement saving player coordinates before teleportation
- Add handling of player logins depending on the time of day
- Implement sound effects for correct/incorrect answers

### Known Issues
- No admin rights check for the `/createroom` command
- No error handling when creating a room
- Holograms and buttons are not properly removed when recreating the room

## Project Structure

```
org.h0x91b.mcTestAi1
├── McTestAi1.java (Main plugin class)
├── config
│   ├── Config.java
│   └── ConfigModule.java
├── commands
│   └── CreateRoomCommand.java
├── events
│   └── EventListener.java
├── managers
│   ├── ClassroomManager.java
│   ├── QuizManager.java
│   ├── DayNightManager.java
│   └── InventoryManager.java
└── resources
    ├── plugin.yml
    └── config.yml
```

## Key Files

1. `McTestAi1.java`: Main plugin class with Guice configuration
2. `Config.java`: Handles plugin configuration
3. `ClassroomManager.java`: Manages classroom creation and structure
4. `QuizManager.java`: Handles quiz functionality
5. `EventListener.java`: Listens for events like block breaking
6. `CreateRoomCommand.java`: Implements the /createroom command
7. `plugin.yml`: Plugin metadata and command definitions
8. `config.yml`: Default configuration file