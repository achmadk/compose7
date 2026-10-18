// Cross-compiled as a klib on any host; keep UIKit usage conservative.
package io.github.compose7.components.forms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectGetMidX
import platform.CoreGraphics.CGRectGetMidY
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.localTimeZone
import platform.UIKit.UIApplication
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIColor
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UINavigationController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewAutoresizingFlexibleBottomMargin
import platform.UIKit.UIViewAutoresizingFlexibleTopMargin
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.modalInPresentation
import platform.UIKit.navigationItem
import platform.UIKit.systemBackgroundColor
import platform.darwin.NSObject

/**
 * Best-effort top-most view controller to present from. Duplicated from
 * `components-overlays` because `internal` does not cross module boundaries;
 * keep the two copies boring and identical.
 */
@OptIn(ExperimentalForeignApi::class)
private fun datePickerTopViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val scenes = application.connectedScenes.filterIsInstance<UIWindowScene>()
    val scene = scenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?: scenes.firstOrNull()
    val windows = scene?.windows?.filterIsInstance<UIWindow>().orEmpty()
    val window = windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
    var top = window?.rootViewController
    while (true) {
        val presented = top?.presentedViewController ?: break
        top = presented
    }
    return top
}

private fun CalendarDate.toNSDate(): NSDate? =
    dateFormatter().dateFromString(formatted())

private fun NSDate.toCalendarDate(): CalendarDate? =
    dateFormatter().stringFromDate(this).let(::parseCalendarDate)

/** Fixed ISO format + POSIX locale so the wheel never reinterprets the date. */
private fun dateFormatter(): NSDateFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd"
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
    timeZone = NSTimeZone.localTimeZone
}

@OptIn(BetaInteropApi::class)
private class DatePickerActions(
    private val onDone: () -> Unit,
    private val onCancel: () -> Unit,
) : NSObject() {
    @ObjCAction
    fun done() = onDone()

    @ObjCAction
    fun cancel() = onCancel()
}

/**
 * `UIBarButtonItem.target` is a weak reference, so the action object must be
 * retained for as long as the sheet can send taps. Removed in `onDispose`.
 */
private val datePickerActionsInFlight = mutableSetOf<Any>()

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
internal actual fun NativeDatePicker(
    selectedDate: CalendarDate?,
    title: String?,
    initialMonth: CalendarDate,
    firstDayOfWeek: Int,
    minDate: CalendarDate?,
    maxDate: CalendarDate?,
    onDateSelected: (CalendarDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)

    DisposableEffect(Unit) {
        val initialDate = selectedDate ?: initialMonth
        val picker = UIDatePicker().apply {
            datePickerMode = UIDatePickerMode.UIDatePickerModeDate
            preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
            locale = NSLocale.currentLocale
            timeZone = NSTimeZone.localTimeZone
            date = initialDate.toNSDate() ?: NSDate()
            minDate?.toNSDate()?.let { minimumDate = it }
            maxDate?.toNSDate()?.let { maximumDate = it }
            sizeToFit()
        }

        val controller = UIViewController().apply {
            view.backgroundColor = UIColor.systemBackgroundColor
        }
        picker.center = CGPointMake(
            CGRectGetMidX(controller.view.bounds),
            CGRectGetMidY(controller.view.bounds),
        )
        picker.autoresizingMask =
            UIViewAutoresizingFlexibleTopMargin or UIViewAutoresizingFlexibleBottomMargin
        controller.view.addSubview(picker)

        val actions = DatePickerActions(
            onDone = {
                val picked = picker.date.toCalendarDate() ?: initialDate
                currentOnDateSelected(picked)
            },
            onCancel = { currentOnDismiss() },
        )
        controller.navigationItem.title = title
        controller.navigationItem.leftBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemCancel,
            target = actions,
            action = NSSelectorFromString("cancel"),
        )
        controller.navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemDone,
            target = actions,
            action = NSSelectorFromString("done"),
        )

        val navigation = UINavigationController(rootViewController = controller).apply {
            modalPresentationStyle = UIModalPresentationPageSheet
            // No swipe-to-dismiss: the field's `expanded` state stays the source
            // of truth, so a half-dismissed wheel can never strand the UI.
            modalInPresentation = true
        }

        val presenter = datePickerTopViewController()
        if (presenter == null) {
            currentOnDismiss()
            onDispose {}
        } else {
            datePickerActionsInFlight += actions
            presenter.presentViewController(navigation, animated = true, completion = null)
            onDispose {
                if (navigation.presentingViewController != null) {
                    navigation.dismissViewControllerAnimated(true, completion = null)
                }
                datePickerActionsInFlight -= actions
            }
        }
    }
}
