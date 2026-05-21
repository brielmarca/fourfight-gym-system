UPDATE graduations
SET deleted_at = NOW(),
    updated_at = NOW()
WHERE deleted_at IS NULL
  AND martial_art_id IN (
    SELECT id
    FROM martial_arts
    WHERE deleted_at IS NULL
      AND LOWER(name) IN (
        'forca & condicionamento',
        'força & condicionamento',
        'forca e condicionamento',
        'força e condicionamento',
        'strength & conditioning'
      )
  );

UPDATE martial_arts
SET deleted_at = NOW(),
    updated_at = NOW()
WHERE deleted_at IS NULL
  AND LOWER(name) IN (
    'forca & condicionamento',
    'força & condicionamento',
    'forca e condicionamento',
    'força e condicionamento',
    'strength & conditioning'
  );
