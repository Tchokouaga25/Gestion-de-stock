-- =====================================================================
-- AfriStock — Produits : statut actif / inactif
-- =====================================================================
ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
