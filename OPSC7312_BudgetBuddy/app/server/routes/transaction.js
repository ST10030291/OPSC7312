const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

//This gets all transactions that match the user id that is currently logged in
router.get('/', async (req, res) => {
  const userId = req.query.userId;

  if (!userId) {
    return res.status(400).json({ error: 'User ID is required' });
  }

  try {
    const snapshot = await db.collection('transactions').where('userId', '==', userId).get();

    //If there are no transctions then a message will be logged
    if (snapshot.empty) {
      return res.status(404).json({ error: 'No transactions found for this user' });
    }

    const transactions = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    res.status(200).json(transactions);
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});
;

//This adds a new transaction to the database and displays a success message if true
router.post('/', async (req, res) => {
  try {
    const newTransactionRef = db.collection('transactions').doc();
    await newTransactionRef.set(req.body);
    res.status(201).json({ message: 'Transaction added successfully' });
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});

module.exports = router;
