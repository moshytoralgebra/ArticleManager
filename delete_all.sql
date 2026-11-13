USE ARTICLES;
GO


DELETE FROM Article;
GO


DELETE FROM Users;
GO


DELETE FROM Category;
GO


DELETE FROM Author;
GO

-- ============================================================
-- Optional: reset IDENTITY counters so new rows start from 1
-- Uncomment the lines below if you want IDs to restart after
-- a full wipe (e.g. during development / test resets).
-- ============================================================
-- DBCC CHECKIDENT ('Article',  RESEED, 0);
-- DBCC CHECKIDENT ('Users',    RESEED, 0);
-- DBCC CHECKIDENT ('Category', RESEED, 0);
-- DBCC CHECKIDENT ('Author',   RESEED, 0);
-- GO
