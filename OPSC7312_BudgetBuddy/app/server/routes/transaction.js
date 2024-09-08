const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

router.get('/', async (req, res) => {
  if (req.query.id) {
    try {
      const transactionDoc = await db.collection('transactions').doc(req.query.id).get();
      if (!transactionDoc.exists) {
        return res.status(404).json({ error: 'Transaction not found' });
      }
      res.status(200).json({ id: transactionDoc.id, ...transactionDoc.data() });
    } catch (error) {
      res.status(500).json({ error: error.toString() });
    }
  } else {
    try {
      const snapshot = await db.collection('transactions').get();
      const transactions = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      res.status(200).json(transactions);
    } catch (error) {
      res.status(500).json({ error: error.toString() });
    }
  }
});

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
