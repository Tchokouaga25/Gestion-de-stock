-- =====================================================================
-- AfriStock — Phase 3 (unification) : les mouvements de stock sont rattachés à un site
-- =====================================================================

ALTER TABLE stock_movements ADD COLUMN site_id BIGINT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_movements_site FOREIGN KEY (site_id) REFERENCES sites (id);
CREATE INDEX idx_stock_movements_site ON stock_movements (site_id);
