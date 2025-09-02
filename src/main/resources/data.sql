
-- Insert test members
INSERT INTO members (name, surname, email, birthday, games_played, rank) VALUES
('John', 'Doe', 'john.doe@email.com', '1985-05-15', 10, 1),
('Jane', 'Smith', 'jane.smith@email.com', '1990-08-22', 8, 2),
('Mike', 'Johnson', 'mike.johnson@email.com', '1988-12-03', 12, 3),
('Sarah', 'Wilson', 'sarah.wilson@email.com', '1992-03-30', 5, 4),
('David', 'Brown', 'david.brown@email.com', '1987-07-14', 7, 5),
('Emily', 'Davis', 'emily.davis@email.com', '1995-11-08', 3, 6);

-- Insert some sample games
INSERT INTO games (player1_id, player2_id, player1_score, player2_score, played_at, 
                  player1_rank_before, player2_rank_before, player1_rank_after, player2_rank_after) 
SELECT 
    p1.id, p2.id, 1, 0, CURRENT_TIMESTAMP - INTERVAL '7' DAY,
    p1.rank, p2.rank, p1.rank, p2.rank
FROM members p1, members p2 
WHERE p1.rank = 1 AND p2.rank = 2;

INSERT INTO games (player1_id, player2_id, player1_score, player2_score, played_at, 
                  player1_rank_before, player2_rank_after, player1_rank_after, player2_rank_after) 
SELECT 
    p1.id, p2.id, 0, 1, CURRENT_TIMESTAMP - INTERVAL '5' DAY,
    p1.rank, p2.rank, p1.rank + 1, p2.rank - 1
FROM members p1, members p2 
WHERE p1.rank = 3 AND p2.rank = 6;