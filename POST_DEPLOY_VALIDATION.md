# Post-Deploy Validation Guide

Después de desplegar en Railway, tienes varias opciones para validar que todo funciona correctamente.

## Opción 1: GitHub Actions Automático (Recomendado) ⭐

**Flujo:**
```
Push to main
    ↓
GitHub Actions Deploy workflow
    ↓
Deploy workflow completa
    ↓
Post-Deploy Validation workflow se dispara automáticamente
    ↓
Ejecuta validate-deployment.sh + test-railway.sh
    ↓
Reporta resultados en GitHub Actions
```

**Archivo:** `.github/workflows/post-deploy-validation.yml`

**Qué hace:**
1. ✅ Espera 60 segundos a que Railway inicie completamente
2. ✅ Ejecuta `validate-deployment.sh` (valida configuración)
3. ✅ Ejecuta `test-railway.sh` (prueba funcionalidad MCP)
4. ✅ Reporta resultados en la sección "Actions" de GitHub

**Ventajas:**
- Automático, sin intervención manual
- Ejecuta después de cada deploy
- Fácil de ver logs en GitHub
- Se integra con tu CI/CD existente

**Ver resultados:**
```bash
# En GitHub
Settings → Actions → "Post-Deploy Validation" → Últimas ejecuciones
```

---

## Opción 2: Manual Local

**Valida configuración:**
```bash
bash validate-deployment.sh https://claude-ia-mcp-tools-java-staging.up.railway.app
```

**Prueba funcionalidad:**
```bash
bash test-railway.sh
```

---

## Opción 3: Procfile Release Process (Avanzado)

Si quieres que los tests se ejecuten **dentro del contenedor Railway** durante el deployment:

```procfile
# .github/workflows/deploy.yml
# Agregar esta línea antes de desplegar a Railway:

release: bash -c "echo 'App starting...'"
web: java -cp target/mcp-users-server-*.jar com.example.mcp.McpWebSocketServer ${PORT:-8080}
```

**Nota:** El proceso `release` se ejecuta ANTES de `web`, útil para migraciones o inicializaciones. No es ideal para tests porque:
- No puedo hacer curl a `localhost` (websocat intenta conectarse al servidor que está iniciando)
- El deployment se bloquea hasta que terminen los tests

---

## Opción 4: Endpoint de Health Check (Avanzado)

Agregar un endpoint `/health` que ejecute validaciones:

```java
// En McpWebSocketServer.java
GET /health → Responde 200 OK
GET /health/full → Ejecuta tests completos
```

Entonces Railway puede usar esto para health checks:
```bash
curl -f https://app.railway.app/health || exit 1
```

---

## Recomendación

**Usa la Opción 1 (GitHub Actions)** porque:
- ✅ No ralentiza el deployment
- ✅ Ejecuta después de que el servidor esté listo
- ✅ Puedes ver los logs en GitHub
- ✅ No tienes que hacerlo manualmente
- ✅ Se integra con tu flujo actual

El workflow ya está creado en `.github/workflows/post-deploy-validation.yml`

Solo asegúrate de que:
1. El workflow de Deploy ya existe (que sí existe)
2. El trigger `workflow_run` está configurado correctamente
3. La URL de Railway es correcta (actualiza si es diferente)

---

## Customización

**Cambiar la URL de Railway:**
```yaml
# post-deploy-validation.yml línea ~37
bash validate-deployment.sh https://TU-URL-AQUI.up.railway.app
```

**Cambiar el tiempo de espera:**
```yaml
# post-deploy-validation.yml línea ~20
sleep 60  # Cambiar a 30, 120, etc.
```

**Agregar más validaciones:**
```yaml
# Agregar pasos adicionales en el job validate-deployment
- name: Custom validation
  run: |
    # Tu comando aquí
```

---

## Troubleshooting

**Los tests fallan con "connection refused":**
- Aumenta el tiempo de espera de 60s a 120s
- Verifica que el Procfile esté correcto
- Revisa los logs de Railway: `railway logs`

**websocat no se instala:**
- GitHub Actions usa `ubuntu-latest` que tiene `apt-get`
- Si tienes problemas, verifica la salida del workflow

**Tests pasan localmente pero fallan en GitHub Actions:**
- Puede ser diferencia de red/firewall
- Verifica la URL exacta en Railway dashboard
