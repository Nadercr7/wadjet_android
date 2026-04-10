# Wadjet Android — Design System (Compose)

> Complete Material 3 theme configuration for Black & Gold identity.
> Every color, font, shape, and component maps to the web design system.

---

## Color Scheme

```kotlin
// WadjetColors.kt
object WadjetColors {
    // Gold palette
    val Gold = Color(0xFFD4AF37)
    val GoldLight = Color(0xFFE5C76B)
    val GoldDark = Color(0xFFB8962E)
    val GoldMuted = Color(0xFFA08520)
    val GoldGlow = Color(0x1FD4AF37)  // 12% opacity

    // Surfaces
    val Night = Color(0xFF0A0A0A)
    val Surface = Color(0xFF141414)
    val SurfaceAlt = Color(0xFF1E1E1E)
    val SurfaceHover = Color(0xFF252525)

    // Borders
    val Border = Color(0xFF2A2A2A)
    val BorderLight = Color(0xFF3A3A3A)

    // Text
    val Text = Color(0xFFF0F0F0)
    val TextMuted = Color(0xFF8A8A8A)
    val TextDim = Color(0xFF7E7E7E)
    val Ivory = Color(0xFFF5F0E8)
    val Sand = Color(0xFFC4A265)
    val Dust = Color(0xFFA89070)

    // Semantic
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFF59E0B)
}
```

### Material 3 Color Scheme Override

```kotlin
// WadjetTheme.kt
private val WadjetDarkColorScheme = darkColorScheme(
    primary = WadjetColors.Gold,
    onPrimary = WadjetColors.Night,
    primaryContainer = WadjetColors.GoldDark,
    onPrimaryContainer = WadjetColors.Ivory,
    secondary = WadjetColors.Sand,
    onSecondary = WadjetColors.Night,
    secondaryContainer = WadjetColors.SurfaceAlt,
    onSecondaryContainer = WadjetColors.Sand,
    tertiary = WadjetColors.GoldLight,
    onTertiary = WadjetColors.Night,
    background = WadjetColors.Night,
    onBackground = WadjetColors.Text,
    surface = WadjetColors.Surface,
    onSurface = WadjetColors.Text,
    surfaceVariant = WadjetColors.SurfaceAlt,
    onSurfaceVariant = WadjetColors.TextMuted,
    surfaceContainerHighest = WadjetColors.SurfaceHover,
    outline = WadjetColors.Border,
    outlineVariant = WadjetColors.BorderLight,
    error = WadjetColors.Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun WadjetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WadjetDarkColorScheme,
        typography = WadjetTypography,
        shapes = WadjetShapes,
        content = content
    )
}
```

**CRITICAL**: Never use `dynamicDarkColorScheme()`. The Wadjet brand is ALWAYS Black & Gold. No Material You dynamic colors.

---

## Typography

### Font Families

```kotlin
// WadjetFonts.kt
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_semibold, FontWeight.SemiBold),
    Font(R.font.playfair_display_bold, FontWeight.Bold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
)

val NotoSansEgyptianHieroglyphs = FontFamily(
    Font(R.font.noto_sans_egyptian_hieroglyphs, FontWeight.Normal),
)

val Cairo = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_medium, FontWeight.Medium),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold, FontWeight.Bold),
)
```

### Type Scale

```kotlin
val WadjetTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        color = WadjetColors.Ivory,
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    ),
)
```

### Arabic Typography Override

```kotlin
// When language is AR, swap Inter → Cairo
fun wadjetTypographyForLang(lang: String): Typography {
    if (lang != "ar") return WadjetTypography
    return WadjetTypography.copy(
        bodyLarge = WadjetTypography.bodyLarge.copy(fontFamily = Cairo),
        bodyMedium = WadjetTypography.bodyMedium.copy(fontFamily = Cairo),
        bodySmall = WadjetTypography.bodySmall.copy(fontFamily = Cairo),
        titleLarge = WadjetTypography.titleLarge.copy(fontFamily = Cairo),
        titleMedium = WadjetTypography.titleMedium.copy(fontFamily = Cairo),
        titleSmall = WadjetTypography.titleSmall.copy(fontFamily = Cairo),
        labelLarge = WadjetTypography.labelLarge.copy(fontFamily = Cairo),
        labelMedium = WadjetTypography.labelMedium.copy(fontFamily = Cairo),
        labelSmall = WadjetTypography.labelSmall.copy(fontFamily = Cairo),
    )
}
```

### Custom Text Styles

```kotlin
// Hieroglyph display style
val HieroglyphStyle = TextStyle(
    fontFamily = NotoSansEgyptianHieroglyphs,
    fontSize = 32.sp,
    color = WadjetColors.Gold,
)

// Gardiner code style
val GardinerCodeStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontSize = 14.sp,
    color = WadjetColors.Sand,
)

// Animated gold gradient text (use Brush)
@Composable
fun GoldGradientText(text: String, style: TextStyle = MaterialTheme.typography.displaySmall) {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Text(
        text = text,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = listOf(WadjetColors.GoldDark, WadjetColors.Gold, WadjetColors.GoldLight, WadjetColors.Gold, WadjetColors.GoldDark),
                start = Offset(offset, 0f),
                end = Offset(offset + 500f, 0f),
            )
        )
    )
}
```

---

## Shapes

```kotlin
val WadjetShapes = Shapes(
    small = RoundedCornerShape(8.dp),     // chips, badges
    medium = RoundedCornerShape(12.dp),   // cards, inputs
    large = RoundedCornerShape(16.dp),    // bottom sheets, dialogs
    extraLarge = RoundedCornerShape(24.dp), // FABs
)
```

---

## Component Library

### WadjetButton (Primary — btn-gold equivalent)

```kotlin
@Composable
fun WadjetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = WadjetColors.Gold,
            contentColor = WadjetColors.Night,
            disabledContainerColor = WadjetColors.GoldMuted.copy(alpha = 0.5f),
            disabledContentColor = WadjetColors.Night.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = WadjetColors.Night,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
```

### WadjetGhostButton (btn-ghost equivalent)

```kotlin
@Composable
fun WadjetGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = WadjetColors.Gold,
        ),
        border = BorderStroke(1.dp, WadjetColors.Gold),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

### WadjetCard (card equivalent)

```kotlin
@Composable
fun WadjetCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = WadjetColors.Surface,
        ),
        border = BorderStroke(1.dp, WadjetColors.Border),
        shape = RoundedCornerShape(12.dp),
        content = content,
    )
}
```

### WadjetCardGlow (card-glow equivalent)

```kotlin
@Composable
fun WadjetCardGlow(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val borderColor by animateColorAsState(
        targetValue = if (isHovered || isPressed) WadjetColors.Gold.copy(alpha = 0.5f) else WadjetColors.Border,
    )
    val elevation by animateDpAsState(
        targetValue = if (isHovered || isPressed) 8.dp else 0.dp,
    )
    
    Card(
        onClick = onClick,
        modifier = modifier.shadow(elevation, RoundedCornerShape(12.dp), spotColor = WadjetColors.GoldGlow),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(containerColor = WadjetColors.Surface),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        content = content,
    )
}
```

### WadjetTextField (input equivalent)

```kotlin
@Composable
fun WadjetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = WadjetColors.TextMuted) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WadjetColors.Gold,
            unfocusedBorderColor = WadjetColors.Border,
            focusedLabelColor = WadjetColors.Gold,
            cursorColor = WadjetColors.Gold,
            focusedTextColor = WadjetColors.Text,
            unfocusedTextColor = WadjetColors.Text,
            errorBorderColor = WadjetColors.Error,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}
```

### WadjetBadge (badge-gold equivalent)

```kotlin
@Composable
fun WadjetBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Gold,
) {
    val (bg, fg) = when (variant) {
        BadgeVariant.Gold -> WadjetColors.Gold.copy(alpha = 0.15f) to WadjetColors.Gold
        BadgeVariant.Muted -> WadjetColors.SurfaceAlt to WadjetColors.TextMuted
        BadgeVariant.Success -> WadjetColors.Success.copy(alpha = 0.15f) to WadjetColors.Success
        BadgeVariant.Error -> WadjetColors.Error.copy(alpha = 0.15f) to WadjetColors.Error
    }
    
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}

enum class BadgeVariant { Gold, Muted, Success, Error }
```

### WadjetTopBar

```kotlin
@Composable
fun WadjetTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge)
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WadjetColors.Night,
            titleContentColor = WadjetColors.Ivory,
        ),
    )
}
```

### WadjetBottomBar

```kotlin
@Composable
fun WadjetBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    NavigationBar(
        containerColor = WadjetColors.Surface,
        contentColor = WadjetColors.TextMuted,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.route) },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WadjetColors.Gold,
                    selectedTextColor = WadjetColors.Gold,
                    unselectedIconColor = WadjetColors.TextMuted,
                    unselectedTextColor = WadjetColors.TextMuted,
                    indicatorColor = WadjetColors.Gold.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
```

### Loading Shimmer

```kotlin
@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
        )
    )
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WadjetColors.Surface,
                        WadjetColors.SurfaceAlt,
                        WadjetColors.Surface,
                    ),
                    start = Offset(translateAnim, 0f),
                    end = Offset(translateAnim + 500f, 0f),
                ),
                shape = RoundedCornerShape(8.dp),
            )
    )
}
```

---

## Animations

### Gold Pulse (pulse-gold equivalent)

```kotlin
@Composable
fun Modifier.goldPulse(): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )
    return this.shadow(
        elevation = 16.dp,
        shape = CircleShape,
        spotColor = WadjetColors.Gold.copy(alpha = alpha),
    )
}
```

### Fade Up (fade-up equivalent)

```kotlin
@Composable
fun FadeUp(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(600, easing = EaseOut)
        ),
        content = content,
    )
}
```

### Ken Burns (for story images)

```kotlin
@Composable
fun KenBurnsImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
            }
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
    )
}
```

---

## Icons

Use **Lucide** icons. Options:
1. `compose-icons/lucide` library (if available)
2. Copy SVG files from web app → convert to Compose `ImageVector` using Android Studio SVG import
3. Use Material Icons as fallback where Lucide equivalent exists

Key icons needed:
| Usage | Lucide Icon | Material Fallback |
|-------|-------------|-------------------|
| Scan | `scan` | `CameraAlt` |
| Dictionary | `book-open` | `MenuBook` |
| Explore | `compass` | `Explore` |
| Stories | `scroll` / `book` | `AutoStories` |
| Chat | `message-circle` | `Chat` |
| Home | `home` | `Home` |
| Profile | `user` | `Person` |
| Settings | `settings` | `Settings` |
| Camera | `camera` | `PhotoCamera` |
| Gallery | `image` | `Image` |
| Favorite | `heart` | `Favorite` |
| Share | `share-2` | `Share` |
| Copy | `copy` | `ContentCopy` |
| Play | `play` | `PlayArrow` |
| Pause | `pause` | `Pause` |
| Mic | `mic` | `Mic` |
| Send | `send` | `Send` |
| Back | `arrow-left` | `ArrowBack` |
| Close | `x` | `Close` |
| Search | `search` | `Search` |
| Filter | `filter` | `FilterList` |
| Map | `map-pin` | `LocationOn` |
| History | `clock` | `History` |
| Volume | `volume-2` | `VolumeUp` |

---

## Status Bar & Navigation Bar

```kotlin
// Always dark theme — gold accent
@Composable
fun WadjetSystemBars() {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(WadjetColors.Night)
        systemUiController.setNavigationBarColor(WadjetColors.Surface)
        systemUiController.statusBarDarkContentEnabled = false  // Light icons on dark bg
    }
}
```

---

## Font Files Required (in `res/font/`)

| File | Source |
|------|--------|
| `playfair_display_semibold.ttf` | Google Fonts |
| `playfair_display_bold.ttf` | Google Fonts |
| `inter_regular.ttf` | Google Fonts |
| `inter_medium.ttf` | Google Fonts |
| `inter_semibold.ttf` | Google Fonts |
| `jetbrains_mono_regular.ttf` | Google Fonts |
| `noto_sans_egyptian_hieroglyphs.ttf` | Web app: `app/static/fonts/` |
| `cairo_regular.woff2` → `.ttf` | Web app: `app/static/fonts/` (convert from woff2) |
| `cairo_medium.ttf` | Google Fonts |
| `cairo_semibold.ttf` | Google Fonts |
| `cairo_bold.ttf` | Google Fonts |
