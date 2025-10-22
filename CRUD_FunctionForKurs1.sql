CREATE OR REPLACE FUNCTION add_user(
  p_login TEXT,
  p_password_hash TEXT,
  p_role_id INT
) RETURNS BIGINT AS $$
DECLARE
  v_user_id BIGINT;
BEGIN
  IF p_login IS NULL OR trim(p_login) = '' THEN
    RAISE EXCEPTION 'Логин не должен быть пустым';
  END IF;


  IF p_password_hash IS NULL OR trim(p_password_hash) = '' THEN
    RAISE EXCEPTION 'Пароль не должен быть пустым';
  END IF;
  
  IF p_role_id IS NULL THEN
    RAISE EXCEPTION 'Роль должна быть указана';
  END IF;
  INSERT INTO users(login, password_hash, role_id)
  VALUES (p_login, p_password_hash, p_role_id)
  RETURNING user_id INTO v_user_id;

  RETURN v_user_id;

EXCEPTION
  WHEN unique_violation THEN
    RAISE EXCEPTION 'Логин "%" уже существует', p_login;
  WHEN foreign_key_violation THEN
    RAISE EXCEPTION 'Роль с id=% не существует', p_role_id;
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION get_users(
  p_limit INT DEFAULT 100,
  p_offset INT DEFAULT 0
) RETURNS TABLE(
  user_id INT,
  login VARCHAR(100),
  role_id INT,
  role_name VARCHAR(100),
  password_hash varchar(100)
) AS $$
BEGIN
  RETURN QUERY
  SELECT u.user_id, u.login, u.role_id, r.role_name, pa
  FROM users u
  LEFT JOIN roles r ON r.role_id = u.role_id
  ORDER BY u.user_id
  LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM get_users(6);


CREATE OR REPLACE FUNCTION update_user(
  p_user_id INT,
  p_login VARCHAR(100) DEFAULT NULL,
  p_password_hash TEXT DEFAULT NULL,
  p_role_id INT DEFAULT NULL
) RETURNS TABLE(
  user_id INT,
  login VARCHAR(100),
  role_id INT
) AS $$
BEGIN
 
  IF p_login IS NOT NULL AND trim(p_login) = '' THEN
    RAISE EXCEPTION 'Логин не должен быть пустым';
  END IF;


  IF p_password_hash IS NOT NULL AND trim(p_password_hash) = '' THEN
    RAISE EXCEPTION 'Пароль не должен быть пустым';
  END IF;

  RETURN QUERY
  UPDATE users u
     SET login=COALESCE(p_login, u.login),
         password_hash=COALESCE(p_password_hash, u.password_hash),
         role_id=COALESCE(p_role_id, u.role_id)
   WHERE u.user_id = p_user_id
  RETURNING u.user_id, u.login, u.role_id;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Пользователь % не найден', p_user_id;
  END IF;

EXCEPTION
  WHEN unique_violation THEN
    RAISE EXCEPTION 'Логин "%" уже существует', p_login;
  WHEN foreign_key_violation THEN
    RAISE EXCEPTION 'Роль с id=% не существует', p_role_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_user(
  p_user_id INT,
  p_login VARCHAR(100) DEFAULT NULL,
  p_password_hash TEXT DEFAULT NULL,
  p_role_id INT DEFAULT NULL
) RETURNS TABLE(
  user_id INT,
  login VARCHAR(100),
  role_id INT
) AS $$
BEGIN
  IF p_login IS NOT NULL AND trim(p_login) = '' THEN
    RAISE EXCEPTION 'Логин не должен быть пустым';
  END IF;

  IF p_password_hash IS NOT NULL AND trim(p_password_hash) = '' THEN
    RAISE EXCEPTION 'Пароль не должен быть пустым';
  END IF;

  RETURN QUERY
  UPDATE users u
     SET login         = COALESCE(p_login, u.login),
         password_hash = COALESCE(p_password_hash, u.password_hash),
         role_id       = COALESCE(p_role_id, u.role_id)
   WHERE u.user_id = p_user_id
  RETURNING u.user_id, u.login, u.role_id;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Пользователь % не найден', p_user_id;
  END IF;

EXCEPTION
  WHEN unique_violation THEN
    RAISE EXCEPTION 'Логин "%" уже существует', p_login;
  WHEN foreign_key_violation THEN
    RAISE EXCEPTION 'Роль с id=% не существует', p_role_id;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION delete_user(p_user_id INT)
RETURNS BOOLEAN AS $$
DECLARE
  v_rows INT;
BEGIN
  DELETE FROM users WHERE user_id = p_user_id;
  GET DIAGNOSTICS v_rows = ROW_COUNT;
  RETURN v_rows > 0;
END;
$$ LANGUAGE plpgsql;

select add_cosmetic_item('testname' , 1 , 1 ,4 , 1 , 'gdsgsdgds' , 'testdecripton' , 1 , 100 , 200)

select * from cosmetic_table


CREATE OR REPLACE FUNCTION add_cosmetic_item(
  p_item_name         VARCHAR(100),
  p_manufacturer_id   INT,
  p_brand_id          INT,
  p_cosmetic_type_id  INT,
  p_certificate_id    INT,
  p_image_path        VARCHAR(255) DEFAULT NULL,
  p_description       TEXT         DEFAULT NULL,
  p_product_status_id INT          DEFAULT NULL,
  p_quantity          INT          DEFAULT 0,
  p_base_price        NUMERIC(10,2) DEFAULT 0
) RETURNS TABLE(
  cosmetic_item_id INT,
  item_name VARCHAR(100),
  quantity INT,
  base_price NUMERIC(10,2),
  price NUMERIC(10,2)
) AS $$
BEGIN
  IF p_item_name IS NULL OR trim(p_item_name) = '' THEN
    RAISE EXCEPTION 'Название товара не должно быть пустым';
  END IF;
  IF p_quantity IS NOT NULL AND p_quantity < 0 THEN
    RAISE EXCEPTION 'Количество не может быть отрицательным';
  END IF;

  RETURN QUERY
  INSERT INTO cosmetic_items(
    item_name, manufacturer_id, brand_id, cosmetic_type_id, certificate_id,
    image_path, description, product_status_id, quantity, base_price
  ) VALUES (
    p_item_name, p_manufacturer_id, p_brand_id, p_cosmetic_type_id, p_certificate_id,
    p_image_path, p_description, p_product_status_id, COALESCE(p_quantity,0), COALESCE(p_base_price,0)
  )
 RETURNING cosmetic_items.cosmetic_item_id AS cosmetic_item_id,
          cosmetic_items.item_name        AS item_name,
          cosmetic_items.quantity         AS quantity,
          cosmetic_items.base_price       AS base_price,
          cosmetic_items.price            AS price; 

END;
$$ LANGUAGE plpgsql;

select update_cosmetic_item(0 , 'vds')
select * from cosmetic_table

CREATE OR REPLACE FUNCTION update_cosmetic_item(
  p_cosmetic_item_id  INT,
  p_item_name         VARCHAR(100) DEFAULT NULL,
  p_manufacturer_id   INT          DEFAULT NULL,
  p_brand_id          INT          DEFAULT NULL,
  p_cosmetic_type_id  INT          DEFAULT NULL,
  p_certificate_id    INT          DEFAULT NULL,
  p_image_path        VARCHAR(255) DEFAULT NULL,
  p_description       TEXT         DEFAULT NULL,
  p_product_status_id INT          DEFAULT NULL,
  p_quantity          INT          DEFAULT NULL,
  p_base_price        NUMERIC(10,2) DEFAULT NULL
) RETURNS TABLE(
  cosmetic_item_id INT,
  item_name VARCHAR(100),
  quantity INT,
  base_price NUMERIC(10,2),
  price NUMERIC(10,2)
) AS $$
BEGIN
  IF p_item_name IS NOT NULL AND trim(p_item_name) = '' THEN
    RAISE EXCEPTION 'Название товара не должно быть пустым';
  END IF;
  IF p_quantity IS NOT NULL AND p_quantity < 0 THEN
    RAISE EXCEPTION 'Количество не может быть отрицательным';
  END IF;

  RETURN QUERY
  UPDATE cosmetic_items ci
     SET item_name         = COALESCE(p_item_name, ci.item_name),
         manufacturer_id   = COALESCE(p_manufacturer_id, ci.manufacturer_id),
         brand_id          = COALESCE(p_brand_id, ci.brand_id),
         cosmetic_type_id  = COALESCE(p_cosmetic_type_id, ci.cosmetic_type_id),
         certificate_id    = COALESCE(p_certificate_id, ci.certificate_id),
         image_path        = COALESCE(p_image_path, ci.image_path),
         description       = COALESCE(p_description, ci.description),
         product_status_id = COALESCE(p_product_status_id, ci.product_status_id),
         quantity          = COALESCE(p_quantity, ci.quantity),
         base_price        = COALESCE(p_base_price, ci.base_price) -- price посчитает триггер
   WHERE ci.cosmetic_item_id = p_cosmetic_item_id
  RETURNING ci.cosmetic_item_id, ci.item_name, ci.quantity, ci.base_price, ci.price;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Товар с id=% не найден', p_cosmetic_item_id;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_cosmetic_item(
  p_cosmetic_item_id INT
)
RETURNS BOOLEAN AS $$
DECLARE
  v_rows INT;
BEGIN
  DELETE FROM cosmetic_items
  WHERE cosmetic_item_id = p_cosmetic_item_id;

  GET DIAGNOSTICS v_rows = ROW_COUNT;

  IF v_rows = 0 THEN
    RAISE EXCEPTION 'Товар с id=% не найден', p_cosmetic_item_id;
  END IF;

  RETURN TRUE;

EXCEPTION
  WHEN foreign_key_violation THEN
    RAISE EXCEPTION
      'Нельзя удалить товар %: существуют связанные записи (в корзинах, заказах, отзывах или избранном)',
      p_cosmetic_item_id;
END;
$$ LANGUAGE plpgsql;






