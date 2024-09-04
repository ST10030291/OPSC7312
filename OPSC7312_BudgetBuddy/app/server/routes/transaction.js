const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

// This gets all transactions
router.get('/transactions', async (req, res) => {
  try {
    const snapshot = await db.collection('transactions').get();
    const Transaction = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    res.json(Transaction);
  } catch (error) {
    res.status(500).send(error.toString());
  }
});

// This will add a new transactions
router.post('/transactions', async (req, res) => {
  try {
    const newTransactionRef = db.collection('transactions').doc();
    await newTransactionRef.set(req.body);
    res.status(201).send('Transaction added successfully');
  } catch (error) {
    res.status(500).send(error.toString());
  }
});

// This will get a single transaction by their assigned ID
router.get('/transactions/:id', async (req, res) => {
  try {
    const TransactionDoc = await db.collection('transactions').doc(req.params.id).get();
    if (!TransactionDoc.exists) {
      return res.status(404).send('Transaction not found');
    }
    res.json({ id: TransactionDoc.id, ...TransactionDoc.data() });
  } catch (error) {
    res.status(500).send(error.toString());
  }
});

module.exports = router;
