CREATE FUNCTION [dbo].[Levenshtein](
    @s nvarchar(4000)
  , @t nvarchar(4000)
  , @max int
)
RETURNS int
WITH SCHEMABINDING
AS
BEGIN
    DECLARE @distance int = 0
          , @v0 nvarchar(4000)
          , @start int = 1
          , @i int, @j int
          , @diag int
          , @left int
          , @sChar nchar
          , @thisJ int
          , @jOffset int
          , @jEnd int
          , @sLen int = datalength(@s) / datalength(left(left(@s, 1) + '.', 1))
          , @tLen int = datalength(@t) / datalength(left(left(@t, 1) + '.', 1))
          , @lenDiff int
    IF (@sLen > @tLen) BEGIN
        SELECT @v0 = @s, @i = @sLen
        SELECT @s = @t, @sLen = @tLen
    SELECT @t = @v0, @tLen = @i
    END
    SELECT @max = ISNULL(@max, @tLen)
     , @lenDiff = @tLen - @sLen
    IF @lenDiff > @max RETURN NULL

    WHILE(@sLen > 0 AND SUBSTRING(@s, @sLen, 1) = SUBSTRING(@t, @tLen, 1))
        SELECT @sLen = @sLen - 1, @tLen = @tLen - 1

        IF (@sLen = 0) RETURN @tLen

        WHILE (@start < @sLen AND SUBSTRING(@s, @start, 1) = SUBSTRING(@t, @start, 1))
            SELECT @start = @start + 1
            IF (@start > 1) BEGIN
                SELECT @sLen = @sLen - (@start - 1)
                , @tLen = @tLen - (@start - 1)

                IF (@sLen <= 0) RETURN @tLen

                SELECT @s = SUBSTRING(@s, @start, @sLen)
                , @t = SUBSTRING(@t, @start, @tLen)
            END

            SELECT @v0 = '', @j = 1
            WHILE (@j <= @tLen) BEGIN
                SELECT @v0 = @v0 + NCHAR(CASE WHEN @j > @max THEN @max ELSE @j END)
                SELECT @j = @j + 1
            END

            SELECT @jOffset = @max - @lenDiff
                , @i = 1
            WHILE (@i <= @sLen) BEGIN
            SELECT @distance = @i
                , @diag = @i - 1
                , @sChar = SUBSTRING(@s, @i, 1)
                , @j = CASE WHEN @i <= @jOffset THEN 1 ELSE @i - @jOffset END
                , @jEnd = CASE WHEN @i + @max >= @tLen THEN @tLen ELSE @i + @max END
            WHILE (@j <= @jEnd) BEGIN
            SELECT @left = UNICODE(SUBSTRING(@v0, @j, 1))
                , @thisJ = @j
            SELECT @distance =
            CASE WHEN (@sChar = SUBSTRING(@t, @j, 1)) THEN @diag
            ELSE 1 + CASE WHEN @diag < @left AND @diag < @distance THEN @diag
                          WHEN @left < @distance THEN @left
                          ELSE @distance
                END    END
            SELECT @v0 = STUFF(@v0, @thisJ, 1, NCHAR(@distance))
                , @diag = @left
                , @j = case when (@distance > @max) AND (@thisJ = @i + @lenDiff) then @jEnd + 2 else @thisJ + 1 end
        END
        SELECT @i = CASE WHEN @j > @jEnd + 1 THEN @sLen + 1 ELSE @i + 1 END
    END
    RETURN CASE WHEN @distance <= @max THEN @distance ELSE NULL END
END