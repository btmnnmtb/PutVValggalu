CREATE OR REPLACE FUNCTION trg_set_item_price()
RETURNS trigger
LANGUAGE plpgsql
AS $$
declare
  v_additional_price NUMERIC(10,2);
BEGIN
  v_additional_price :=
    COALESCE((SELECT m.manufacturer_price FROM manufacturers m WHERE m.manufacturer_id = NEW.manufacturer_id),0)
  + COALESCE((SELECT b.brand_price        FROM brands b        WHERE b.brand_id        = NEW.brand_id),0)
  + COALESCE((SELECT t.cosmetic_type_price FROM cosmetic_types t WHERE t.cosmetic_type_id = NEW.cosmetic_type_id),0)
  + COALESCE((SELECT q.certificate_price  FROM quality_certificates q WHERE q.certificate_id = NEW.certificate_id),0);
  
    NEW.price := COALESCE(NEW.base_price, 0) + v_additional_price;
  RETURN NEW;
END;
$$;


CREATE OR REPLACE FUNCTION update_cart_total()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  v_cart_id INT;
  v_total   NUMERIC(10,2);
BEGIN
  v_cart_id := COALESCE(NEW.cart_id, OLD.cart_id);

  SELECT COALESCE(SUM(ci.quantity * i.price), 0)
    INTO v_total
  FROM cart_items ci
  JOIN cosmetic_items i ON i.cosmetic_item_id = ci.cosmetic_item_id
  WHERE ci.cart_id = v_cart_id;

  UPDATE carts
     SET total = v_total
   WHERE cart_id = v_cart_id;
  RETURN NULL;
END;
$$;
select * from carts
select * from cart_items_view
select * from cosmetic_table


CREATE OR REPLACE FUNCTION item_rating(p_item_id INT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
  v_avg DOUBLE PRECISION;
BEGIN
  SELECT AVG(rating)::DOUBLE PRECISION
    INTO v_avg
  FROM comments
  WHERE cosmetic_item_id = p_item_id
    AND rating IS NOT NULL;

  UPDATE cosmetic_items
     SET rating = COALESCE(v_avg, 0)
   WHERE cosmetic_item_id = p_item_id;
END;
$$;


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




