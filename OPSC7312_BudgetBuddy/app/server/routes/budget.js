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

// Get remaining budget for a user
router.get('/remaining-budget', async (req, res) => {
  const userId = req.query.userId;
  if (!userId) {
    return res.status(400).json({ error: 'User ID is required' });
  }

  try {
    // Fetch the total budget for the user
    const budgetSnapshot = await db.collection('budgets').where('userId', '==', userId).get();
    if (budgetSnapshot.empty) {
      return res.status(404).json({ error: 'No budget found for this user' });
    }

    const budgetDoc = budgetSnapshot.docs[0].data();
    const totalBudget = budgetDoc.totalBudget;

    // Calculate total transactions amount
    const transactionsSnapshot = await db.collection('transactions').where('userId', '==', userId).get();
    let totalSpent = 0;

    transactionsSnapshot.forEach(doc => {
      const transaction = doc.data();
      totalSpent += transaction.amount;
    });

    // Calculate remaining budget
    const remainingBudget = totalBudget - totalSpent;

    res.status(200).json({ remainingBudget });
  } catch (error) {
    res.status(500).json({ error: error.toString() });
  }
});

module.exports = router;
