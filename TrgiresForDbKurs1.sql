CREATE TRIGGER set_item_price_biub
BEFORE INSERT OR UPDATE OF manufacturer_id, brand_id, cosmetic_type_id, certificate_id , base_price
ON cosmetic_items
FOR EACH ROW EXECUTE FUNCTION trg_set_item_price();


CREATE TRIGGER touch_carts_on_item_price
AFTER INSERT OR UPDATE OR DELETE
ON cart_items
FOR EACH ROW
EXECUTE FUNCTION update_cart_total();

CREATE TRIGGER comments_aiud_recalc_rating
AFTER INSERT OR UPDATE OF rating, cosmetic_item_id OR DELETE
ON comments
FOR EACH ROW
EXECUTE FUNCTION trg_comments_recalc_item_rating();

CREATE TRIGGER users_after_insert_log
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION trg_log_register();