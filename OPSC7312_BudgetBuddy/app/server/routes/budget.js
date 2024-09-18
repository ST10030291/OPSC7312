const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const db = admin.firestore();

router.get('/', async (req, res) => {
  const userId = req.query.userId;
  try {
    if (!userId) {
      return res.status(400).json({ error: 'User ID is required' });
    }
    const snapshot = await db.collection('budgets').where('userId', '==', userId).get();
    if (snapshot.empty) {
      return res.status(404).json({ error: 'No budgets found for this user' });
    }
    const budgets = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    res.status(200).json(budgets);
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});

router.post('/', async (req, res) => {
  try {
    const newBudgetRef = db.collection('budgets').doc();
    await newBudgetRef.set(req.body);
    res.status(201).json({ id: newBudgetRef.id, message: 'Budget added successfully' });
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});

router.put('/:id', async (req, res) => {
  const budgetId = req.params.id;
  const { spentBudget } = req.body; // Ensure that spentBudget is sent in the request body

  try {
    // Check if the budget exists
    const budgetRef = db.collection('budgets').doc(budgetId);
    const doc = await budgetRef.get();

    if (!doc.exists) {
      return res.status(404).json({ error: 'Budget not found' });
    }

    // Update the spentBudget field
    await budgetRef.update({ spentBudget });

    res.status(200).json({ message: 'Budget updated successfully' });
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});

module.exports = router;
