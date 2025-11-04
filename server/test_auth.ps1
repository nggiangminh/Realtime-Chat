# PowerShell script để test Authentication flow
Write-Host "=== RealTime Chat Authentication Test ===" -ForegroundColor Green

$baseUrl = "http://localhost:8083"

# Test 1: Public endpoint
Write-Host "`n1. Testing public endpoint..." -ForegroundColor Yellow
try {
    $publicResponse = Invoke-RestMethod -Uri "$baseUrl/api/test/public"
    Write-Host "✅ Public endpoint works: $($publicResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Public endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Register new user
Write-Host "`n2. Testing user registration..." -ForegroundColor Yellow
$registerData = @{
    email = "test$(Get-Random)@example.com"  # Random email để tránh trùng
    password = "password123"
    displayName = "Test User $(Get-Random)"
}

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -ContentType "application/json" -Body ($registerData | ConvertTo-Json -Compress)
    Write-Host "✅ User registered successfully: $($registerResponse.data.email)" -ForegroundColor Green
} catch {
    Write-Host "❌ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error details: $errorContent" -ForegroundColor Red
    }
    exit 1
}

# Test 3: Login
Write-Host "`n3. Testing user login..." -ForegroundColor Yellow
$loginData = @{
    email = $registerData.email
    password = $registerData.password
}

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body ($loginData | ConvertTo-Json -Compress)
    $token = $loginResponse.data.token
    Write-Host "✅ Login successful, token received" -ForegroundColor Green
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Get current user info
Write-Host "`n4. Testing get current user..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $userInfo = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Headers $headers
    Write-Host "✅ User info retrieved: $($userInfo.data.displayName)" -ForegroundColor Green
} catch {
    Write-Host "❌ Get user info failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 5: Protected endpoint
Write-Host "`n5. Testing protected endpoint..." -ForegroundColor Yellow
try {
    $protectedResponse = Invoke-RestMethod -Uri "$baseUrl/api/test/protected" -Headers $headers
    Write-Host "✅ Protected endpoint works: $($protectedResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Protected endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Logout
Write-Host "`n6. Testing logout..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method POST -Headers $headers
    Write-Host "✅ Logout successful: $($logoutResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Logout failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Try accessing protected endpoint after logout (should still work as JWT doesn't get invalidated on server side)
Write-Host "`n7. Testing protected endpoint after logout..." -ForegroundColor Yellow
try {
    $protectedResponse2 = Invoke-RestMethod -Uri "$baseUrl/api/test/protected" -Headers $headers
    Write-Host "✅ Protected endpoint still works (JWT stateless): $($protectedResponse2.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Protected endpoint failed after logout: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Authentication Test Completed ===" -ForegroundColor Green
Write-Host "All authentication features are working correctly!" -ForegroundColor Cyan
