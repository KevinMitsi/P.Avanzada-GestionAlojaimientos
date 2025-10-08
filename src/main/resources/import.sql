-- import.sql para MySQL - Sistema de Alojamientos
-- Datos de prueba ordenados por dependencias

-- 1) Ciudades (sin dependencias)
INSERT INTO cities (name, country) VALUES ('Medellín', 'Colombia');
INSERT INTO cities (name, country) VALUES ('Bogotá', 'Colombia');
INSERT INTO cities (name, country) VALUES ('Cartagena', 'Colombia');
INSERT INTO cities (name, country) VALUES ('Cali', 'Colombia');
INSERT INTO cities (name, country) VALUES ('Barcelona', 'España');

-- 2) Usuarios (sin dependencias) - ACTUALIZADO para múltiples roles
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Admin Principal', 'admin@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573001234567', '1985-03-15', 'Administrador del sistema', true, true, '2024-01-01 10:00:00', false);
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('María González', 'maria@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573112345678', '1980-07-22', 'Anfitriona experimentada', true, true, '2024-01-15 09:30:00', false);
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Carlos Rodríguez', 'carlos@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573223456789', '1975-11-08', 'Propietario de apartamentos turísticos', true, true, '2024-01-20 14:15:00', false);
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Ana Martínez', 'ana@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573334567890', '1988-04-12', 'Arquitecta convertida en anfitriona', true, true, '2024-01-25 11:45:00', false);
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Laura Pérez', 'laura@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573445678901', '1992-12-05', 'Viajera frecuente', true, true, '2024-02-05 08:30:00', false);
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Diego Ramírez', 'diego@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573556789012', '1989-06-18', 'Fotógrafo profesional', true, true, '2024-02-10 12:15:00', false);

-- 2.1) NUEVO: Roles de usuarios (múltiples roles por usuario)
INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (2, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (2, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (3, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (3, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (4, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (4, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (5, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (6, 'USER');

-- 3) Perfiles de anfitriones (depende de usuarios)
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (2, 'Alojamientos María', true, '2024-01-15 10:00:00');
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (3, 'Apartamentos Centro', true, '2024-01-20 15:00:00');
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (4, 'Design Stays', true, '2024-01-25 12:00:00');

-- 4) Alojamientos (depende de usuarios y ciudades) - ACTUALIZADO con coordenadas embebidas
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Apartamento Moderno en El Poblado', 'Hermoso apartamento en Medellín con vista espectacular', 'Cra 35 #10A-52, El Poblado', 6.2088, -75.5648, 120000.00, 4, true, false, '2024-01-16 10:00:00', 2, 1);
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Casa Colonial en La Candelaria', 'Auténtica casa colonial en Bogotá, totalmente restaurada', 'Calle 12C #4-69, Bogotá', 4.5981, -74.0758, 90000.00, 6, true, false, '2024-01-21 11:30:00', 3, 2);
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Loft en Zona Rosa', 'Loft de diseño contemporáneo con terraza', 'Cra 13 #85-32, Bogotá', 4.6551, -74.0593, 180000.00, 2, true, false, '2024-01-26 14:15:00', 4, 2);

-- 5) Documentos de usuarios
INSERT INTO user_documents (user_id, document_url) VALUES (2, 'https://docs.example.com/maria_cedula.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (2, 'https://docs.example.com/maria_rut.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (3, 'https://docs.example.com/carlos_cedula.pdf');

-- 6) Servicios de alojamientos (depende de alojamientos)
INSERT INTO accommodation_services (accommodation_id, service) VALUES (1, 'WiFi gratuito');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (1, 'Aire acondicionado');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (1, 'Cocina equipada');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (2, 'Desayuno incluido');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (2, 'Chimenea');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (3, 'Jacuzzi');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (3, 'Gimnasio');

-- 7) Imágenes (depende de alojamientos) - ACTUALIZADO sin user_id
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at, accommodation_id) VALUES ('https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=300', 'accommodation_1_primary', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_300/accommodation_1_primary', true, '2024-01-16 10:15:00', 1);
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at, accommodation_id) VALUES ('https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=300', 'accommodation_2_primary', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_300/accommodation_2_primary', true, '2024-01-21 11:45:00', 2);
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at, accommodation_id) VALUES ('https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800', 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=300', 'accommodation_3_primary', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_300/accommodation_3_primary', true, '2024-01-26 14:30:00', 3);

-- 7.1) NUEVO: Fotos de perfil de usuarios (sin accommodation_id, sin user_id)
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1494790108755-2616b612b619?w=400', 'https://images.unsplash.com/photo-1494790108755-2616b612b619?w=150', 'user_2_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_2_profile', true, '2024-01-15 10:30:00');
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', 'user_3_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_3_profile', true, '2024-01-20 15:30:00');
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150', 'user_5_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_5_profile', true, '2024-02-05 09:00:00');

-- 7.2) Actualizar usuarios con sus imágenes de perfil
UPDATE users SET profile_image_id = 4 WHERE id = 2;
UPDATE users SET profile_image_id = 5 WHERE id = 3;
UPDATE users SET profile_image_id = 6 WHERE id = 5;

-- 8) Reservaciones (depende de alojamientos y usuarios) - ACTUALIZADO con nuevos campos
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-03-01', '2024-03-05', 4, 480000.00, 'COMPLETED', '2024-02-20 10:00:00', 1, 5);
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-03-10', '2024-03-14', 4, 360000.00, 'COMPLETED', '2024-02-25 14:30:00', 2, 6);
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-04-01', '2024-04-05', 4, 720000.00, 'CONFIRMED', '2024-03-15 11:20:00', 3, 5);
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, cancelled_at, motivo_cancelacion, cancelled_by, accommodation_id, user_id) VALUES ('2024-05-01', '2024-05-05', 4, 480000.00, 'CANCELLED', '2024-04-15 10:00:00', '2024-04-20 15:30:00', 'Cambio de planes de viaje', 'USER', 1, 6);

-- 9) Pagos (depende de reservaciones)
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (480000.00, 'CARD', 'COMPLETED', '2024-02-20 10:15:00', 1);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (360000.00, 'PAYPAL', 'COMPLETED', '2024-02-25 14:45:00', 2);

-- 10) Comentarios (depende de reservaciones, alojamientos y usuarios) - ACTUALIZADO con HostReply embebido
INSERT INTO comments (rating, comment_text, created_at, is_moderated, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (5, 'Excelente apartamento, muy limpio y cómodo. La ubicación es perfecta.', '2024-03-06 15:30:00', false, 'Gracias Laura! Me alegra que hayas disfrutado tu estadía.', '2024-03-06 18:45:00', 1, 1, 5);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (4, 'La casa colonial es hermosa. El único detalle es un poco de ruido.', '2024-03-15 12:20:00', false, 'Gracias Diego, instalaremos nuevas ventanas para mejorar.', '2024-03-15 16:30:00', 2, 2, 6);

-- 11) Favoritos (depende de usuarios y alojamientos)
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-02-28 14:20:00', 5, 1);
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-03-05 11:15:00', 6, 2);

-- 12) Notificaciones (depende de usuarios)
INSERT INTO notifications (title, body, type, metadata, is_read, created_at, user_id) VALUES ('Reservación Confirmada', 'Tu reservación en "Apartamento Moderno en El Poblado" fue confirmada.', 'NEW_RESERVATION', '{"reservation_id":1}', false, '2024-02-20 10:20:00', 5);
INSERT INTO notifications (title, body, type, metadata, is_read, created_at, user_id) VALUES ('Nuevo Comentario', 'Has recibido un nuevo comentario en tu alojamiento.', 'NEW_COMMENT', '{"comment_id":1}', true, '2024-03-06 18:50:00', 2);

-- 13) Tokens de reseteo de contraseña (depende de usuarios)
INSERT INTO password_reset_tokens (token_hash, expires_at, used, created_at, user_id) VALUES ('$2a$10$abcd1234567890abcdef1234567890abcdef123456', '2024-12-15 15:45:00', false, '2024-12-14 15:45:00', 5);
INSERT INTO password_reset_tokens (token_hash, expires_at, used, created_at, user_id) VALUES ('$2a$10$efgh1234567890abcdef1234567890abcdef123456', '2024-12-16 15:45:00', true, '2024-12-15 15:45:00', 6);
-- ============================================
-- Fin del import.sql
