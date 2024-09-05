const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

// This gets all budgets
router.get('/budgets', async (req, res) => {
  try {
    const snapshot = await db.collection('budgets').get();
    const budgets = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    res.json(budgets);
  } catch (error) {
    res.status(500).send(error.toString());
  }
});

// This will add a new budget
router.post('/budgets', async (req, res) => {
  try {
    const newBudgetRef = db.collection('budgets').doc();
    await newBudgetRef.set(req.body);
    res.status(201).send('Budget added successfully');
  } catch (error) {
    res.status(500).send(error.toString());
  }
});

// This will get a single budget by their assigned ID
router.get('/budgets/:id', async (req, res) => {
  try {
    const budgetDoc = await db.collection('budgets').doc(req.params.id).get();
    if (!budgetDoc.exists) {
      return res.status(404).send('Budget not found');
    }
    res.json({ id: budgetDoc.id, ...budgetDoc.data() });
  } catch (error) {
    res.status(500).send(error.toString());
  }
});

module.exports = router;
