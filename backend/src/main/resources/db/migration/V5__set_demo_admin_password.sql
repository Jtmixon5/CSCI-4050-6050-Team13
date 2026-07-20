-- Demo administrator credentials: admin@cinema.com / Admin123!
-- Change this password immediately in any non-demo environment.
UPDATE users
SET password_hash = '$2a$12$ig6Aw25/5VJEGB.YLFtcLOC7tStJ2p3VXYtyf4jvtH7q3Aiz5Ex6O'
WHERE email = 'admin@cinema.com';
