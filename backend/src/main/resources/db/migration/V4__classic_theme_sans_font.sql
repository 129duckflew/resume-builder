-- Classic theme: switch the default font stack from serif (Times New Roman)
-- to a modern sans-serif stack that unifies Latin + CJK rendering.
UPDATE themes
SET variables_schema = replace(
        variables_schema,
        '"default":"''Times New Roman'', Times, serif"',
        '"default":"''Helvetica Neue'', ''Liberation Sans'', Arial, ''PingFang SC'', ''WenQuanYi Zen Hei'', ''Noto Sans CJK SC'', ''Microsoft YaHei'', sans-serif"'
    )
WHERE id = 'classic';
