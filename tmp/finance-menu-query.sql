SELECT child.id, child.name, child.path, child.component, child.component_name, child.status, child.visible, child.sort
FROM system_menu child
WHERE child.deleted = b'0'
  AND child.parent_id = (
    SELECT root.id
    FROM system_menu root
    WHERE root.parent_id = 0 AND root.path = '/finance' AND root.deleted = b'0'
    ORDER BY root.id
    LIMIT 1
  )
ORDER BY child.sort, child.id;
