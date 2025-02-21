package com.kaajjo.libresudoku.ui.more.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaajjo.libresudoku.BuildConfig
import com.kaajjo.libresudoku.R
import com.kaajjo.libresudoku.ui.components.PreferenceRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navigateBack: () -> Unit,
    navigateOpenSourceLicenses: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_arrow_back_24),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(56.dp),
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Divider()

            PreferenceRow(
                title = stringResource(R.string.about_version),
                subtitle = BuildConfig.VERSION_NAME,
                painter = painterResource(R.drawable.ic_outline_info_24),
            )

            val uriHandler = LocalUriHandler.current
            PreferenceRow(
                title = stringResource(R.string.about_github_project),
                painter = painterResource(R.drawable.ic_github_24dp),
                onClick = {
                    uriHandler.openUri("https://github.com/kaajjo/Libre-Sudoku")
                }
            )

            PreferenceRow(
                title = stringResource(R.string.help_translate),
                painter = painterResource(R.drawable.ic_round_translate_24),
                onClick = {
                    uriHandler.openUri("https://hosted.weblate.org/engage/libresudoku/")
                }
            )

            PreferenceRow(
                title = stringResource(R.string.libraries_licenses_title),
                painter = painterResource(R.drawable.ic_outline_info_24),
                onClick = navigateOpenSourceLicenses
            )
        }
    }
}