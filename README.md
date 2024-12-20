# Projet : Rustyze

## **Description**

Ce projet consiste en une application mobile qui permet aux utilisateurs de :

- **Noter et consulter des avis sur les voitures et motos** : Aider les acheteurs à évaluer les véhicules avant leur achat.
- **Consulter des critiques professionnelles** : Les critiques ont une section dédiée pour renforcer leur crédibilité.

L'objectif est de fournir une plateforme équitable et fiable pour évaluer les véhicules, combinant les avis des utilisateurs et des experts.

---

## **Fonctionnalités principales**

### **1. Recherche et Filtres**

- Rechercher des modèles spécifiques (voitures/motos).
- Appliquer des filtres pour affiner les recherches :
  - Type de véhicule.
  - Consommation.
  - Prix.
  - Marque.

### **2. Notation des Véhicules**

- **Utilisateurs :**
  - Donner une note en étoiles (Star Rating).
  - Ajouter des commentaires et lire les avis des autres utilisateurs.
- **Critiques professionnelles :**
  - Publier des critiques détaillées.
  - Les critiques sont mises en avant et regroupées dans une section dédiée pour différencier leur avis.

### **3. Statistiques des Véhicules**

- Informations techniques disponibles pour chaque modèle :
  - Consommation moyenne.
  - Taux de survie (fiabilité).
  - Prix moyen sur le marché.

---

## **Catégorisation des Avis**

- **Critiques professionnelles :**
  - Avis mis en évidence dans une section spéciale.
  - Attribution de commentaires négatifs ou positifs selon le véhicule.
- **Utilisateurs :**
  - Note globale en étoiles.
  - Accès à un résumé des avis et des points forts/faibles des modèles.

---

## **Technologies suggérées**

- **Frontend :**
  - Kotlin pour le développement mobile android.
- **Base de données :**
  - NoSQL (Firestore et local) pour stocker les données des utilisateurs, critiques, et statistiques des véhicules.
- **Intégration :**
  - APIs pour récupérer des données techniques sur les véhicules (exemple : bases de données publiques ou fournisseurs tiers).

---

## **Schéma fonctionnel**

1. Les utilisateurs peuvent **chercher des modèles** et **consulter leurs notes**.
2. Deux catégories de notation :
   - **Notes utilisateurs** : Basées sur l'expérience personnelle.
   - **Critiques professionnelles** : Données par des experts crédibles.
3. Les statistiques techniques complètent les avis pour une vue globale.

---

## **Vision**

Créer une plateforme collaborative où la communauté et les experts se réunissent pour fournir une évaluation transparente des véhicules, aidant ainsi les consommateurs à faire des choix éclairés.
