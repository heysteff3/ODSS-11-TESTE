-- Baseline migration for SustentaFome
IF NOT EXISTS (
    SELECT 1
    FROM sys.objects
    WHERE object_id = OBJECT_ID(N'[dbo].[flyway_placeholder]')
      AND type = 'U'
)
BEGIN
    CREATE TABLE dbo.flyway_placeholder (
        id INT PRIMARY KEY
    );
END;
