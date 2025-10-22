CREATE OR REPLACE PROCEDURE sp_add_to_cart(
  p_user_id INT,
  p_cosmetic_item_id INT,
  p_qty INT
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_cart_id INT;
  v_stock   INT;
BEGIN
  IF p_qty IS NULL OR p_qty <= 0 THEN
    RAISE EXCEPTION 'Количество должно быть > 0';
  END IF;

  SELECT quantity INTO v_stock
  FROM cosmetic_items
  WHERE cosmetic_item_id = p_cosmetic_item_id
  FOR UPDATE;
  IF v_stock IS NULL THEN
    RAISE EXCEPTION 'Товар % не найден', p_cosmetic_item_id;
  END IF;
  IF v_stock < p_qty THEN
    RAISE EXCEPTION 'Недостаточно на складе: нужно %, есть %', p_qty, v_stock;
  END IF;
  UPDATE cosmetic_items
  SET quantity = quantity - p_qty
  WHERE cosmetic_item_id = p_cosmetic_item_id;
  
  SELECT cart_id INTO v_cart_id
  FROM carts WHERE user_id = p_user_id;

  IF v_cart_id IS NULL THEN
    INSERT INTO carts(user_id) VALUES (p_user_id) RETURNING cart_id INTO v_cart_id;
  END IF;


  IF EXISTS (SELECT 1 FROM cart_items WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id) THEN
    UPDATE cart_items
      SET quantity = quantity + p_qty
    WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id;
  ELSE
    INSERT INTO cart_items(cart_id, cosmetic_item_id, quantity)
    VALUES (v_cart_id, p_cosmetic_item_id, p_qty);
  END IF;
END;
$$;
call sp_add_to_cart(1 ,1 , 1)

CREATE OR REPLACE PROCEDURE sp_remove_from_cart_units(
  p_user_id INT,
  p_cosmetic_item_id INT,
  p_remove_qty INT
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_cart_id INT;
  v_old_qty INT;
  v_new_qty INT;
BEGIN
  IF p_remove_qty IS NULL OR p_remove_qty <= 0 THEN
    RAISE EXCEPTION 'Количество к удалению должно быть > 0';
  END IF;

  SELECT cart_id INTO v_cart_id FROM carts WHERE user_id = p_user_id;
  IF v_cart_id IS NULL THEN
    RAISE EXCEPTION 'У пользователя % нет корзины', p_user_id;
  END IF;

  SELECT quantity INTO v_old_qty
  FROM cart_items
  WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id;

  IF v_old_qty IS NULL THEN
    RETURN;
  END IF;

  v_new_qty := v_old_qty - p_remove_qty;

  UPDATE cosmetic_items
     SET quantity = quantity + LEAST(p_remove_qty, v_old_qty)
   WHERE cosmetic_item_id = p_cosmetic_item_id;

  IF v_new_qty <= 0 THEN
    DELETE FROM cart_items
     WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id;
  ELSE
    UPDATE cart_items
       SET quantity = v_new_qty
     WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id;
  END IF;
END;
$$;
CREATE OR REPLACE PROCEDURE sp_remove_from_cart(
  p_user_id INT,
  p_cosmetic_item_id INT
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_cart_id INT;
  v_old_qty INT;
BEGIN
  SELECT cart_id INTO v_cart_id FROM carts WHERE user_id = p_user_id;
  IF v_cart_id IS NULL THEN
    RAISE EXCEPTION 'У пользователя % нет корзины', p_user_id;
  END IF;

  SELECT quantity INTO v_old_qty
  FROM cart_items
  WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id;

  IF v_old_qty IS NULL THEN
    RETURN; 
  END IF;


  UPDATE cosmetic_items
     SET quantity = quantity + v_old_qty
   WHERE cosmetic_item_id = p_cosmetic_item_id;

  DELETE FROM cart_items
   WHERE cart_id = v_cart_id AND cosmetic_item_id = p_cosmetic_item_id;
END;
$$;



call sp_add_to_cart(1 , 1 , 5)
call sp_remove_from_cart_units(1 , 1 , 1)
call sp_remove_from_cart(1 , 1)

select * from carts

select * from cosmetic_items
select * from cart_items_view



CREATE OR REPLACE PROCEDURE add_to_favourite(
  p_user_id INT,
  p_cosmetic_item_id int
)
LANGUAGE plpgsql
AS $$ 
BEGIN
	if exists (
		select 1 from favourites
		where user_id = p_user_id
			and cosmetic_item_id = p_cosmetic_item_id
	) then 
	delete from favourites
	 WHERE user_id = p_user_id
      AND cosmetic_item_id = p_cosmetic_item_id;
    RAISE NOTICE 'Товар удалён из избранного';
 ELSE
   
    INSERT INTO favourites(user_id, cosmetic_item_id)
    VALUES (p_user_id, p_cosmetic_item_id);
    RAISE NOTICE 'Товар добавлен в избранное';
  END IF;
  
END;
$$;

call add_to_favourite(2, 1)
select * from favourites


