import os
import re

ROOT = r"d:\ruoyi-vue-pro"

# 1. Find all Java files extending TenantBaseDO
tenant_base_do_files = []
# 2. Find all Java files referencing TenantContextHolder
tenant_context_files = []
# 3. Find all Java files referencing TenantIgnore annotation
tenant_ignore_files = []
# 4. Find all Java files referencing tenantId in code
tenant_id_ref_files = []

skip_dirs = {'target', '.git', 'node_modules', '.idea', 'production', 'sql', 'scripts', 'docs', 'artifacts'}

for root, dirs, files in os.walk(ROOT):
    dirs[:] = [d for d in dirs if d not in skip_dirs]
    for f in files:
        if not f.endswith('.java'):
            continue
        fpath = os.path.join(root, f)
        try:
            content = open(fpath, encoding='utf-8', errors='ignore').read()
        except:
            continue
        
        rel = os.path.relpath(fpath, ROOT)
        
        if 'extends TenantBaseDO' in content:
            tenant_base_do_files.append(rel)
        if 'TenantContextHolder' in content:
            tenant_context_files.append(rel)
        if '@TenantIgnore' in content:
            tenant_ignore_files.append(rel)
        if 'tenantId' in content or 'tenant_id' in content:
            tenant_id_ref_files.append(rel)

print("=== 1. 继承 TenantBaseDO 的实体类 ===")
print(f"   共 {len(tenant_base_do_files)} 个文件")
for f in sorted(tenant_base_do_files):
    print(f"   {f}")

print("\n=== 2. 引用 TenantContextHolder 的文件 ===")
print(f"   共 {len(tenant_context_files)} 个文件")
for f in sorted(tenant_context_files):
    print(f"   {f}")

print("\n=== 3. 使用 @TenantIgnore 注解的文件 ===")
print(f"   共 {len(tenant_ignore_files)} 个文件")
for f in sorted(tenant_ignore_files):
    print(f"   {f}")

print("\n=== 4. 引用 tenantId/tenant_id 的文件（不含上面已列出的） ===")
already = set(tenant_base_do_files + tenant_context_files + tenant_ignore_files)
extra = [f for f in tenant_id_ref_files if f not in already]
print(f"   共 {len(extra)} 个文件")
for f in sorted(extra):
    print(f"   {f}")

# 5. Count by module
print("\n=== 5. 按模块统计 tenant 引用量 ===")
module_counts = {}
for f in tenant_id_ref_files:
    parts = f.split(os.sep)
    if len(parts) > 1:
        module = parts[0]
    else:
        module = 'root'
    module_counts[module] = module_counts.get(module, 0) + 1

for m, c in sorted(module_counts.items(), key=lambda x: -x[1]):
    print(f"   {m}: {c} 个文件")

# 6. Framework layer
print("\n=== 6. 框架层 tenant 相关文件 ===")
fw_files = [f for f in tenant_id_ref_files if 'yudao-framework' in f]
print(f"   共 {len(fw_files)} 个文件")
for f in sorted(fw_files):
    print(f"   {f}")