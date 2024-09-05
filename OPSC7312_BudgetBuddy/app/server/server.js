const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
const serviceAccount = require('./config/opsc7312-budgetbuddy-firebase-adminsdk-pk4i1-f281034b3d.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const app = express();
const port = 3000;

app.use(bodyParser.json());

app.get('/', (req, res) => {
  res.send('Hello World!');
});

const budgetRoutes = require('./routes/budget');
app.use('/api', budgetRoutes);

const transactionRoutes = require('./routes/transaction');
app.use('/api', transactionRoutes);

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}/`);
});

