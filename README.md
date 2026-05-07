# Campus Bank Project Notes

## Overview

This repository contains my **Campus Bank** Android application for **CIS4034 Mobile App Development**.

The project was developed in **Kotlin** using **Jetpack Compose**. It includes:

- a splash screen
- a combined authentication screen for login and registration
- a dashboard screen
- a transfer screen
- Firebase Authentication and Cloud Firestore integration
- per-user local persistence with DataStore
- biometric login support
- a secure HTTPS exchange-rate web service

The coursework package name is:

- `uk.ac.tees.mad.F5250116`

## Sprint Summary

### Sprint 1: Project Setup and Initial UI

The first sprint focused on setting up the Android Studio project, organising the structure, and building the first user-facing screens.

Completed work:

- project created with the coursework package name
- Compose navigation structure added
- splash screen implemented
- authentication screen structure created
- app branding and theme applied

Main code references:

- app root and navigation: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:95`
- splash screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:219`
- authentication screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:246`

Suggested evidence:

- splash screen running in the app
- authentication page
- GitHub sprint issues and milestones

### Sprint 2: Core Screens and App Flow

The second sprint focused on linking the screens together and making the application usable from sign-in through to money transfer.

Completed work:

- dashboard screen added
- transfer screen added
- registration kept on the same authentication screen
- receipt flow simplified into the transfer screen
- navigation updated to keep the app within the intended 4-screen structure

Main code references:

- dashboard screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:493`
- transfer screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:585`
- success confirmation card: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:675`

Suggested evidence:

- dashboard showing balance and account information
- transfer form before submission
- transfer success message after submission

### Sprint 3: Firebase Authentication and Biometric Login

The third sprint focused on secure authentication and backend integration.

Completed work:

- Firebase Authentication configured
- Firestore profile storage added
- registration with email, first name, last name, and PIN
- login with email and PIN
- session restore support
- biometric login linked to a specific account on the device

Main code references:

- Firebase auth repository: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:14`
- Firebase registration: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:23`
- Firebase sign-in: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:82`
- session restore: `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:108`
- ViewModel login: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:91`
- ViewModel registration: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:95`
- biometric login in ViewModel: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:136`
- biometric prompt usage: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:780`

Suggested evidence:

- Firebase Authentication users list
- registration section expanded on the auth screen
- login form
- biometric prompt on a supported device

### Sprint 4: Per-User Data and Transfer Processing

The fourth sprint focused on making the app behave correctly for different users and implementing transfer logic.

Completed work:

- local data separated by Firebase user ID
- different users have different balances and transfer histories
- transfer validation added
- successful transfers deduct from the current user balance
- user profile data is synced into local app state

Main code references:

- BankPreferences model: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:27`
- per-user preferences flow: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:51`
- save profile: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:80`
- save transfer: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:96`
- transfer logic in ViewModel: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:201`
- shared authentication handling: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:233`
- profile sync: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:297`

Suggested evidence:

- different accounts showing different balances
- different accounts showing different recent transfers
- transfer validation messages

### Sprint 5: Recent Transfers, Permissions, Security, and Polish

The fifth sprint focused on refinement, device support, permissions, and documentation of storage and web services.

Completed work:

- recent transfers dropdown added to dashboard
- live exchange rates shown on dashboard
- biometric availability messaging added for unsupported devices
- biometric credential storage hardened against corrupted local key data
- permissions reviewed and kept minimal

Main code references:

- recent transfers card: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:636`
- biometric availability handling: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:453`
- biometric credential repository: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BiometricCredentialsRepository.kt:13`
- encrypted credential recovery: `app/src/main/java/uk/ac/tees/mad/F5250116/data/BiometricCredentialsRepository.kt:43`
- exchange rate service: `app/src/main/java/uk/ac/tees/mad/F5250116/data/ExchangeRateService.kt:15`
- exchange rate API object: `app/src/main/java/uk/ac/tees/mad/F5250116/data/ExchangeRateService.kt:23`
- manifest permissions: `app/src/main/AndroidManifest.xml:5`

Suggested evidence:

- recent transfers expanded on dashboard
- biometric option visible on emulator with unavailability message
- biometric option working on physical device
- Android manifest permission lines

## Code Evidence

Useful code areas for screenshots or discussion:

- splash screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:219`
- authentication screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:246`
- dashboard screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:493`
- transfer screen: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:585`
- recent transfers card: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:636`
- biometric prompt: `app/src/main/java/uk/ac/tees/mad/F5250116/BankApp.kt:780`
- login function: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:91`
- registration function: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:95`
- biometric login function: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:136`
- transfer function: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:201`
- session restore: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:274`
- profile sync: `app/src/main/java/uk/ac/tees/mad/F5250116/BankViewModel.kt:297`

## Local Data Storage

The app stores some data locally on the device to support persistence and the user experience.

### DataStore

Stored per Firebase user ID:

- first name
- last name
- full name
- email address
- generated account number
- balance
- recent transfers
- cached exchange rates
- last exchange-rate update date

Main code references:

- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:27`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:51`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:96`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BankPreferencesRepository.kt:111`

### Encrypted biometric credentials

Stored only when biometric login is enabled for an account:

- linked email address
- linked 6-digit PIN

Main code references:

- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BiometricCredentialsRepository.kt:13`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BiometricCredentialsRepository.kt:18`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/BiometricCredentialsRepository.kt:25`

## Third-Party Services

### Firebase

Firebase is used for:

- account registration
- account login
- current session detection
- restoring an authenticated session
- storing and retrieving user profile data

Main code references:

- `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:14`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:23`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:82`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/FirebaseAuthRepository.kt:108`

### Frankfurter Exchange Rate API

The Frankfurter API is used as the external HTTPS web service for live currency exchange rates.

It supports:

- GBP to USD rate display
- GBP to EUR rate display
- refreshing exchange-rate information on the dashboard

Main code references:

- `app/src/main/java/uk/ac/tees/mad/F5250116/data/ExchangeRateService.kt:15`
- `app/src/main/java/uk/ac/tees/mad/F5250116/data/ExchangeRateService.kt:23`

## Permissions

The app uses only the permissions needed for its implemented features:

- `android.permission.INTERNET`
- `android.permission.USE_BIOMETRIC`

Code reference:

- `app/src/main/AndroidManifest.xml:5`

## Project Notes

- Sprint Plans 1 and 2 and Sprint Reviews 1 and 2 match the earlier setup and screen-flow stages of the project.
- The later repository history shows the Firebase, biometric, transfer, and security improvements added after those first documents.
- The GitHub repository milestones and issues can be used as evidence of sprint planning and sprint tracking.
