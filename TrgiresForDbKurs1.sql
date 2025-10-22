CREATE TRIGGER set_item_price_biub
BEFORE INSERT OR UPDATE OF manufacturer_id, brand_id, cosmetic_type_id, certificate_id , base_price
ON cosmetic_items
FOR EACH ROW EXECUTE FUNCTION trg_set_item_price();


CREATE TRIGGER touch_carts_on_item_price
AFTER INSERT OR UPDATE OR DELETE
ON cart_items
FOR EACH ROW
EXECUTE FUNCTION update_cart_total();

CREATE OR REPLACE FUNCTION trg_comments_recalc_item_rating()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    PERFORM item_rating(NEW.cosmetic_item_id);

  ELSIF TG_OP = 'UPDATE' THEN
    IF NEW.cosmetic_item_id IS DISTINCT FROM OLD.cosmetic_item_id THEN
      PERFORM item_rating(OLD.cosmetic_item_id);
      PERFORM item_rating(NEW.cosmetic_item_id);
    ELSIF NEW.rating IS DISTINCT FROM OLD.rating THEN
      PERFORM item_rating(NEW.cosmetic_item_id);
    END IF;

  ELSIF TG_OP = 'DELETE' THEN
    PERFORM item_rating(OLD.cosmetic_item_id);
  END IF;

  RETURN NULL; 
END;
$$;