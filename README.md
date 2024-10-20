# Anarcomarombismo

Anarcomarombismo is an Android application designed to help users track their daily calorie intake and manage their workout routines efficiently. With a user-friendly interface, the app makes it simple to log both food consumption and exercise, helping users stay on top of their fitness and nutritional goals. Additionally, it supports importing and exporting of both workout sessions and daily calorie data, making it a versatile tool for fitness management.

## Features

1. **Daily Calorie Tracking**
   - Log daily food intake, including calories and nutritional information (protein, carbs, fats, etc.).
   - Calculate total calories consumed and display results in an easy-to-read format.

2. **Food Database**
   - Access a comprehensive list of foods with detailed nutritional values, including macronutrients like protein, carbohydrates, and fats.
   - Search for foods and add them to your daily log effortlessly.

3. **Exercise Management**
   - Create, edit, and delete personalized workout routines to fit your fitness objectives.
   - Log exercises and track progress over time, making adjustments as needed to optimize your training.

4. **Import/Export Data**
   - Import and export training data, daily calorie logs, and nutritional information for backup, sharing, or transferring between devices.

5. **User-Friendly Interface**
   - Intuitive design that simplifies navigation and data entry, ensuring users can easily log food and exercises.
   - Responsive layout that adapts to different screen sizes and devices.

6. **Data Persistence**
   - User data is stored locally using JSON, providing offline access and data retention.
   - Data is securely stored and can be easily exported for safekeeping or to move between devices.

## Technologies Used

- **Kotlin**: The main programming language, known for its modern, concise syntax, ideal for Android development.
- **Android SDK**: A comprehensive set of tools and libraries for building robust Android applications.
- **JSON**: Used for storing and retrieving food and exercise data, ensuring efficient serialization and deserialization of user data.
- **Coroutines**: Handle asynchronous tasks, ensuring smooth UI interactions by preventing the main thread from blocking.
- **Android Architecture Components**: Tools like ViewModel and LiveData are used to manage UI-related data in a lifecycle-aware manner, ensuring data persistence even during configuration changes.

## Installation

To get started with the Anarcomarombismo app, follow these steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/WillGolden80742/Anarcomarombismo.git
   ```

2. **Open the Project**: Launch Android Studio and open the project.

3. **Build the Project**: Sync the Gradle files to download dependencies, then build the project.

4. **Run the App**: Connect an Android device or use an emulator to test the app.

## Usage

1. **Launch the App**: Start the Anarcomarombismo app on your Android device.
2. **Daily Calorie Tracking**: Navigate to the daily calories section, where you can log your food intake. Search for foods in the database and add them to your daily log, tracking both calorie consumption and macronutrient breakdown.
3. **Exercise Management**: Use the exercise management feature to create, edit, and track your workout routines. Log exercises as you complete them and review your progress over time.
4. **Import/Export Data**: Utilize the import/export feature to load and save both training sessions and daily calorie logs. This helps when transferring data between devices or creating backups.
5. **Nutritional Information**: Browse the food database to access detailed nutritional information for various foods, helping you make informed dietary choices.

## Contributing

Contributions to this project are welcomed! To contribute, follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Make your changes and commit them (`git commit -m 'Add some feature'`).
4. Push to your branch (`git push origin feature/YourFeature`).
5. Submit a pull request for review.

## License

This project is licensed under the MIT License. For more details, refer to the [LICENSE](LICENSE) file.
