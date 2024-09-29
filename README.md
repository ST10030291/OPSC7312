# OPSC7312 - Part 1
# Budget Buddy Application 

# Overview
Effective money management is crucial for personal as well as professional achievement 
in the chaotic economy of today. "Budget Buddy" is a unique budgeting application that we 
developed after realising the growing demand for useful solutions to assist people in 
managing their personal finances. The goal of "Budget Buddy" is to offer a simple, practical, 
and efficient way for individuals to manage their personal finances. 

## Team Members
- Avish Judnarain, ST10030291
- Kaushil Dajee, ST10079389
- Jereshan Sinan, ST10033851
- Eben Nkulu Mwema, ST10091324

## Installation
Clone this repository to your local machine using git clone <repository_url>.
Open the project in Android Studio.
Build and run the app on your Android device or emulator.

## Device requirements
Users will need an android device running an android version between Android 7 (Android API Level 24), codename "Nougat" amd Android 12L (Android API Level 32), codename "Snow Cone". 

## How to use our App
When you open the app you will be required to either Sign Up for a new account or Log In. 
You are also able to login with google SSO via your google account.

### Dashboard Page
On the Dashboard page you will be greeted by a message with your desired username,
A floating island will display your budget/available budget and transaction count for the current month.
Under this you will find a breakdown of your budget for the month with different categories and their allocated amounts.
And under that you will find the monthly budget insight which is a visual display of how well you are keeping to your budget,
in order for it to populate you have to set a budget and then log atleast 1 transaction and it will display your budget (blue circle)
and your available budget (red circle). Keys are provided below the visual.

### Pop-Up Menu
Via this pop up you are able to do two things, Set a budget for the month or log a transaction. To set a budget you have to enter
the total budget and add categories as needed with the amount allocation and click create. To log a transaction you have to enter the
name of the transaction, the amount and the category which it falls under. This will also then populate the monthly budget insight.

### Transactions Page
This is where you can view a full list of all recorded transaction in detail with its name, amount, date and category.

### Statistics Page
This page displays the total amount of money you've spent on a specific category in the form of percentage values inside two different graphs (a Pie Chart and a horizontal bar graph). The percentage values are calculated based on your total spending amount. Users also have the ability to save the Pie Chart and horizontal bar graph displayed on the screen to their devices storage for viewing at a later stage. Lastly, users can filter the chart and graph data by monthly dates.

### Profile Page
On this page you are able to view information about your account as well as change any settings related to your account. 
You can view your accounts associated email, amount of total budgets you have made including the total amount and also a history
of these budgets. In regard to the settings you can change your profile picture on this page and also change the language 
setting of your application and swap between english and afrikaans.

# Features:
## Required Feature 1 - (single sign-on (SSO))
The user must be able to register and log in to the app using single sign-on (SSO). Implemented with Google.
## Required Feature 1 - (Settings)
The user must be able to change their settings in the app. The user is able to change various setting in the account settings such as the language from English to Afrikaans.
## Required Feature 1 - (REST API)
Connect to a REST API you created that is connected to your database. 

## Feature 1 - Set a Budget
Via the pop-up botton and menu a user is able to set a budget for the current month by inputting the total budget and entering categories and their allocated funds.
## Feature 2 - Log a Transaction
Via the same pop-up button users can log any transactions they make in real life which will track their expenses. In the future this could be connected to the users 
banking app.
## Feature 3 - Widget
A widget identical to the floating island on the dashboard is displayed as a widget on the users home screen,
so that they are able to quickly view Availble budget and budget for the month as wel ass amount of transactions for the month.
## Feature 4 - Language Accessibility
The app is currently translatable to one alternative South African language which is Afrikaans making the app more accessible.
## Feature 5 - Profile Pic
To ensure customization and personalization of users and the app, users are able to add their own profile picture.
## Feature 6 - Save Graph & Chart 
Users can save their graphs and charts to their devices storage via one of two buttons on the statistics page.

## Comprehensive report detailing the purpose of the app, its design considerations and the utilisation of GitHub and GitHub Actions
As a team, we used GitHub to collaborate effectively on developing our application. We created a shared repository to host the project's source code, allowing each member to clone, modify, and contribute to the project. Through branching, team members could work on different features or bug fixes simultaneously without interfering with each other's progress. By using GitHub Actions, we automated tests and deployment processes, making the development cycle more efficient. This collaborative approach helped us maintain version control, improve team coordination, and deliver a well-organized, high-quality application.

Thank you for using our application :) 
