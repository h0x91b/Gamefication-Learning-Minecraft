# McTestAi1 - Educational Minecraft Plugin

## Core Concept

McTestAi1 is a Minecraft plugin that transforms the game into an interactive educational platform. It creates a unique day-night cycle where night is dedicated to learning, and day is for applying knowledge and relaxation.

## How it works

1. **Day Cycle:**
  - Players freely explore the world and engage in normal Minecraft activities.
  - The duration of the day depends on players' success in nightly quizzes.
  - Day length starts at 30 seconds and increases by 20 seconds for each correct answer from the previous night.

2. **Night Cycle:**
  - All players are automatically teleported to a specially created "classroom".
  - The classroom is a protected space where building and PvP are disabled.
  - A quiz with questions on various topics is conducted in the classroom.
  - Night duration is fixed at 90 seconds.

3. **Quiz System:**
  - Questions are displayed as holograms in the center of the classroom.
  - Answer options appear on signs above buttons.
  - Players answer by clicking on the corresponding button.
  - Questions are randomly selected from a configurable pool, with measures to prevent frequent repetition.

4. **Reward System:**
  - Correct answers add extra time to the next day cycle.
  - This incentivizes players to actively participate in quizzes and learn.

5. **Multilingual Support:**
  - Questions can be configured in multiple languages (currently Russian, English, and Hebrew).
  - The plugin can switch between languages, allowing for diverse educational experiences.

6. **Automatic Classroom Management:**
  - The classroom is automatically created at a specified height when the plugin starts.
  - It includes features like a blackboard, lighting, and quiz buttons.
  - The room is protected from modifications to maintain its structure.

7. **Player Management:**
  - Players are automatically teleported to the classroom at night and back to their previous locations during the day.
  - New players joining during night time are teleported directly to the classroom.

## Key Features

- Automatic creation and management of the classroom.
- Dynamic day-night cycle with quiz-based day duration.
- Multi-language support for questions (Russian, English, Hebrew).
- Protection system for the classroom (no block breaking, no PvP).
- Automatic player teleportation based on time of day.
- Configurable quiz questions via YAML file.
- Visual feedback for quiz answers (button disappearance, cooldowns).

## Project Goals

The main aim is to create an engaging educational environment where learning becomes a natural part of the gameplay. The plugin strives to make education interesting and interactive, rewarding players for their knowledge and curiosity.

## Target Audience

- Educational institutions using Minecraft in teaching.
- Minecraft servers wanting to add an educational element.
- Parents and teachers looking for ways to combine gaming and learning.

## Future Development

- Adding new question types and tasks.
- Integrating with external APIs for dynamic question generation.
- Expanding the reward system and achievements.
- Creating tools for teachers to manage content.

McTestAi1 aims to make learning in Minecraft both fun and effective, blending gameplay with educational objectives.
