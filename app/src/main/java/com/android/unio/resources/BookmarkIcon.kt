import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


/*
From https://composeicons.com/icons/heroicons/bookmark
Licensed under the MIT license
Copyright (c) Tailwind Labs, Inc.
 */
 val BookmarkIcon: ImageVector
  get() {
    if (_Bookmark != null) {
      return _Bookmark!!
    }
    _Bookmark = ImageVector.Builder(
      name = "Bookmark",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = null,
        fillAlpha = 1.0f,
        stroke = SolidColor(Color(0xFF0F172A)),
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero
      ) {
        moveTo(17.5933f, 3.32241f)
        curveTo(18.6939f, 3.4501f, 19.5f, 4.399f, 19.5f, 5.507f)
        verticalLineTo(21f)
        lineTo(12f, 17.25f)
        lineTo(4.5f, 21f)
        verticalLineTo(5.50699f)
        curveTo(4.5f, 4.399f, 5.3061f, 3.4501f, 6.4067f, 3.3224f)
        curveTo(8.2416f, 3.1095f, 10.108f, 3f, 12f, 3f)
        curveTo(13.892f, 3f, 15.7584f, 3.1095f, 17.5933f, 3.3224f)
        close()
      }
    }.build()
    return _Bookmark!!
  }

private var _Bookmark: ImageVector? = null
