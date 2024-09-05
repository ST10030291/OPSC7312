const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

router.get('/', async (req, res) => {
  if (req.query.id) {
    try {
      const budgetDoc = await db.collection('budgets').doc(req.query.id).get();
      if (!budgetDoc.exists) {
        return res.status(404).json({ error: 'Budget not found' });
      }
      res.status(200).json({ id: budgetDoc.id, ...budgetDoc.data() });
    } catch (error) {
      res.status(500).json({ error: error.toString() });
    }
  } else {
    try {
      const snapshot = await db.collection('budgets').get();
      const budgets = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      res.status(200).json(budgets);
    } catch (error) {
      res.status(500).json({ error: error.toString() });
    }
  }
});

router.post('/', async (req, res) => {
  try {
    const newBudgetRef = db.collection('budgets').doc();
    await newBudgetRef.set(req.body);
    res.status(201).json({ message: 'Budget added successfully' });
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});

module.exports = router;
