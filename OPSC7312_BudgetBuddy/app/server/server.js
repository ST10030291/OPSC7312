const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const path = require('path');

//environment variables from vercel will be initialised here
admin.initializeApp({
  credential: admin.credential.cert({
    projectId: process.env.FIREBASE_PROJECT_ID,
    privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, "\n"),
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL
  })
});

const app = express();
const port = process.env.PORT || 3000;

app.use(bodyParser.json());

const budgetRoutes = require('./routes/budget');
const transactionRoutes = require('./routes/transaction');

app.use('/api/budgets', budgetRoutes);
app.use('/api/transactions', transactionRoutes);

app.get('/', (req, res) => {
  res.send('Hello World!');
});


module.exports = app;
