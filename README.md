# BankApp Sprint README

## Project Overview

This repository contains the **Campus Bank** Android application developed for **CIS4034 Mobile App Development**.
The app was built in **Kotlin** using **Jetpack Compose** and integrates **Firebase Authentication**, **Cloud Firestore**, **DataStore persistence**, **biometric authentication**, and a secure **HTTPS exchange rate API**.

This README links the sprint work to the actual code so it can be used as evidence in:

- Sprint plans
- Sprint reviews
- Presentation slides
- Screen recordings
- Viva or tutor discussion

The package name used for the coursework is:

- `uk.ac.tees.mad.F5250116`

## Sprint Summary

### Sprint 1: Project Setup and Initial Authentication UI

**Goal**

Set up the Android project, establish GitHub version control, and create the initial authentication flow foundation.

**Implemented outcomes**

- Android project created and structured for Compose
- Splash screen added
- Core authentication screen added
- Navigation foundation added
- App theme and branding updated

**Main code evidence**

- App entry and navigation root: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:86`
- Splash screen implementation: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:190`
- Authentication screen implementation: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:217`

**What to screenshot**

- Splash screen UI on emulator
- Authentication screen UI
- Navigation from splash to authentication
- GitHub repository milestone and sprint issues

### Sprint 2: Core Screens and Functional User Flow

**Goal**

Connect the major screens and make the app usable from authentication through to dashboard and transfer flow.

**Implemented outcomes**

- Dashboard screen created
- Transfer screen created
- Authentication and registration merged into one clean screen
- Transfer and receipt merged into one screen
- 4-screen flow made clearer for ICA compliance

**Main code evidence**

- Dashboard screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:312`
- Transfer screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:406`
- Inline receipt section: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:458`

**What to screenshot**

- Dashboard showing balance and account details
- Transfer page before submission
- Transfer page after submission showing inline receipt
- Expanded registration section within the authentication screen

### Sprint 3: Firebase Authentication and Secure Login

**Goal**

Implement secure backend authentication and profile support using Firebase.

**Implemented outcomes**

- Firebase configured in Gradle
- Email and PIN login added
- Registration added with first name, last name, email, and PIN
- Firestore profile support added
- Session restoration added
- Biometric login added

**Main code evidence**

- Firebase dependencies: `app/build.gradle.kts:54`
- Firebase auth repository: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:23`
- Firebase sign-in logic: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:82`
- Session restore logic: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:108`
- ViewModel login: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:85`
- ViewModel register: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:121`
- Biometric login logic: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:162`
- Biometric prompt: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:622`

**What to screenshot**

- Firebase console Authentication users list
- Authentication screen login fields
- Expanded registration form
- Biometric prompt on emulator/device
- Firestore `users` collection

### Sprint 4: Per-User Banking Data and Transfer Logic

**Goal**

Make the banking data behave like a real multi-user app and implement actual transfer logic.

**Implemented outcomes**

- Data stored per Firebase user ID
- Separate balances for different users
- Separate transfer history for different users
- Balance deduction after successful transfer
- Validation for empty and invalid transfer fields
- Biometric setting stored per user

**Main code evidence**

- Per-user preferences flow: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:51`
- Transfer save logic: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:96`
- Biometric toggle persistence: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:119`
- User-specific data separation in ViewModel: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:56`
- Transfer submission validation: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:226`
- Transfer write call: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:247`

**What to screenshot**

- Two different Firebase users in Authentication
- User A balance and transfer history
- User B balance and transfer history
- Transfer validation examples

### Sprint 5: Recent Transfers, API Integration, Security, and Final Polish

**Goal**

Improve realism, polish the user experience, and strengthen security justification for assessment.

**Implemented outcomes**

- Recent transfers list added
- Dashboard recent transfers dropdown added
- Transfer screen recent receipt history added
- Up to 5 recent transfers stored
- Exchange rates retrieved through secure HTTPS
- `INTERNET` permission added only where needed
- Security notes integrated into UI

**Main code evidence**

- Recent transfer model: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:20`
- Recent transfer list in preferences: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:37`
- Recent transfer dashboard card: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:468`
- Receipt history card: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:507`
- Exchange rate persistence: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:111`
- Exchange rate refresh logic: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:194`
- Internet permission: `app/src/main/AndroidManifest.xml:5`

**What to screenshot**

- Dashboard with expanded recent transfers
- Transfer screen showing cleared form and stacked receipt history
- Exchange rate section on dashboard
- Android manifest permission line

## Code Snippets To Screenshot

Use the following code locations as your strongest evidence screenshots.

### Splash Screen

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:190`

### Authentication Screen

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:217`

### Login Function

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:85`

### Registration Function

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:121`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:23`

### Firebase Connection

- `app/build.gradle.kts:54`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:82`

### Biometric Authentication

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:162`
- `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:622`

### Per-User Data Separation

- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:51`
- `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:56`

### Transfer Logic

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:226`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:96`

### Recent Transfers

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:468`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:37`

### Receipt History

- `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:507`

### Security and Permissions

- `app/src/main/AndroidManifest.xml:5`
- `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:393`

## Recommended Screen Recording Order

For your demo video, use this order:

1. Splash screen appears
2. Authentication screen opens
3. Expand registration section
4. Register a new user
5. Log in as that user
6. Show dashboard balance and exchange rates
7. Open recent transfers dropdown
8. Navigate to transfer screen
9. Complete one transfer
10. Show inline receipt
11. Complete another transfer
12. Show stacked receipt history
13. Log out
14. Log in as another user to prove per-user separation
15. Demonstrate biometric login if available

## Notes For Sprint Documentation

- Sprint Plan 1 and Sprint Review 1 align with the initial authentication and setup phase.
- Sprint Plan 2 and Sprint Review 2 align with the connected screen and backend-authentication phase.
- Sprints 3 to 5 can now be documented directly from the implemented repository structure, Firebase integration, and sprint issues created in GitHub.

## GitHub Sprint Evidence

The GitHub repository now includes sprint milestones and sprint issues for:

- Sprint 1
- Sprint 2
- Sprint 3
- Sprint 4
- Sprint 5

These can be used as agile evidence alongside the sprint plan and sprint review documents.
