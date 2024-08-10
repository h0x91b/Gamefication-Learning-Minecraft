# Todo List

This file contains tasks, improvements, and features that are planned or need to be addressed in future updates.

## High Priority

- [ ] Prevent from renaming signs in the classroom
- [x] After pressing incorrect button all buttons became not active anymore
- [ ] Implement inventory management system to lock player inventories during night/quiz time
- [ ] Add sound effects for correct/incorrect quiz answers
- [x] Implement admin rights check for classroom creation and management commands
- [x] Add handling of player logins depending on the time of day (teleport to classroom if night)
- [x] Improve error handling when creating a classroom
- [x] Prohibit any building or block placement within the classroom
- [x] Disable player-vs-player damage within the classroom
- [x] Implement a 3-second cooldown with button disappearance after an incorrect answer
- [x] Add a 1-second cooldown with button disappearance after a correct answer
- [x] Move all questions and answers from code to a YAML configuration file
- [x] Implement language selection support for Russian, Hebrew, and English
- [x] Show earned time after each correct answer
- [x] Optimize classroom cleanup process to occur only once at game start
- [x] Improve quiz button setup and management
- [ ] Randomize order of answers e.g. buttons
- [ ] Consider implementing a weighted randomization system to slightly favor questions that haven't been asked in a long time
- [ ] Add a configuration option to control how many nights should pass before questions can be repeated
- [ ] Add to the best player some cool effect for the next day

## Medium Priority

- [ ] Integrate with an external API for dynamic question generation
- [ ] Implement a leaderboard system for quiz scores
- [ ] Add visual effects for correct/incorrect answers (particle effects, etc.)
- [x] Create a configuration option for customizing day/night durations
- [ ] Implement a system for saving and loading classroom locations across server restarts

## Low Priority

- [x] Add more varied questions to the hardcoded question set
- [ ] Implement difficulty scaling for quizzes based on player performance
- [ ] Create a GUI for quiz answer selection instead of buttons
- [ ] Add custom models for classroom furniture (desks, chairs, etc.)
- [ ] Implement a "raise hand" feature for players to ask questions during quiz time

## Bug Fixes

- [ ] Address potential race conditions in the day/night cycle transitions
- [x] Ensure proper cleanup of holograms and buttons when recreating the classroom

## Optimization

- [ ] Profile and optimize the classroom creation process for larger classrooms
- [ ] Implement caching for frequently accessed configuration values

## Documentation

- [ ] Create comprehensive JavaDocs for all classes and methods
- [ ] Write a user guide for server administrators on how to set up and use the plugin
- [ ] Document the quiz API for potential future extensions

## Testing

- [ ] Implement unit tests for core functionality (DayNightManager, QuizManager, etc.)
- [ ] Set up integration tests to ensure proper plugin behavior in a Minecraft server environment

Remember to update this list as tasks are completed or new ideas come up!