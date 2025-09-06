package name.abuchen.portfolio.ui.views.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Supplier;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import name.abuchen.portfolio.model.Dashboard;
import name.abuchen.portfolio.model.Dashboard.Widget;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.snapshot.PerformanceIndex;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.UIConstants;
import name.abuchen.portfolio.ui.util.Colors;
import name.abuchen.portfolio.ui.util.LabelOnly;
import name.abuchen.portfolio.ui.util.StringToCurrencyConverter;
import name.abuchen.portfolio.ui.util.swt.ColoredLabel;
import name.abuchen.portfolio.util.Interval;
import name.abuchen.portfolio.util.TextUtil;

public class FIREWidget extends WidgetDelegate<FIREWidget.FIREData>
{
    public static class FIREData
    {
        private Money fireNumber;
        private Money currentValue;
        private Money monthlySavings;
        private double twror;
        private double yearsToFire;
        private LocalDate targetDate;

        public Money getFireNumber()
        {
            return fireNumber;
        }

        public void setFireNumber(Money fireNumber)
        {
            this.fireNumber = fireNumber;
        }

        public Money getCurrentValue()
        {
            return currentValue;
        }

        public void setCurrentValue(Money currentValue)
        {
            this.currentValue = currentValue;
        }

        public Money getMonthlySavings()
        {
            return monthlySavings;
        }

        public void setMonthlySavings(Money monthlySavings)
        {
            this.monthlySavings = monthlySavings;
        }

        public double getTwror()
        {
            return twror;
        }

        public void setTwror(double twror)
        {
            this.twror = twror;
        }

        public double getYearsToFire()
        {
            return yearsToFire;
        }

        public void setYearsToFire(double yearsToFire)
        {
            this.yearsToFire = yearsToFire;
        }

        public LocalDate getTargetDate()
        {
            return targetDate;
        }

        public void setTargetDate(LocalDate targetDate)
        {
            this.targetDate = targetDate;
        }
    }

    private static class FIRENumberConfig implements WidgetConfig
    {
        private final WidgetDelegate<?> delegate;
        private Money fireNumber;

        public FIRENumberConfig(WidgetDelegate<?> delegate)
        {
            this.delegate = delegate;

            String fireNumberStr = delegate.getWidget().getConfiguration().get(Dashboard.Config.FIRE_NUMBER.name());
            if (fireNumberStr != null && !fireNumberStr.isEmpty())
            {
                try
                {
                    long amount = Long.parseLong(fireNumberStr);
                    this.fireNumber = Money.of(delegate.getClient().getBaseCurrency(), amount);
                }
                catch (NumberFormatException e)
                {
                    this.fireNumber = null; // No default, show placeholder
                }
            }
            else
            {
                this.fireNumber = null; // No default, show placeholder
            }
        }

        public Money getFireNumber()
        {
            return fireNumber;
        }

        public void setFireNumber(Money fireNumber)
        {
            this.fireNumber = fireNumber;
            delegate.getWidget().getConfiguration().put(Dashboard.Config.FIRE_NUMBER.name(),
                            String.valueOf(fireNumber.getAmount()));
            delegate.update();
            delegate.getClient().touch();
        }

        @Override
        public void menuAboutToShow(IMenuManager manager)
        {
            String display = fireNumber != null
                            ? ((FIREWidget) delegate).formatMoneyShort(fireNumber,
                                            delegate.getClient().getBaseCurrency())
                            : Messages.LabelFIREClickToSet;
            manager.appendToGroup(DashboardView.INFO_MENU_GROUP_NAME,
                            new LabelOnly(Messages.LabelFIRENumber + ": " + display));
        }

        @Override
        public String getLabel()
        {
            String display = fireNumber != null
                            ? ((FIREWidget) delegate).formatMoneyShort(fireNumber,
                                            delegate.getClient().getBaseCurrency())
                            : Messages.LabelFIREClickToSet;
            return Messages.LabelFIRENumber + ": " + display;
        }

    }

    private static class FIREMonthlySavingsConfig implements WidgetConfig
    {
        private final WidgetDelegate<?> delegate;
        private Money monthlySavings;

        public FIREMonthlySavingsConfig(WidgetDelegate<?> delegate)
        {
            this.delegate = delegate;

            String monthlySavingsStr = delegate.getWidget().getConfiguration()
                            .get(Dashboard.Config.FIRE_MONTHLY_SAVINGS.name());
            if (monthlySavingsStr != null && !monthlySavingsStr.isEmpty())
            {
                try
                {
                    long amount = Long.parseLong(monthlySavingsStr);
                    this.monthlySavings = Money.of(delegate.getClient().getBaseCurrency(), amount);
                }
                catch (NumberFormatException e)
                {
                    this.monthlySavings = null;
                }
            }
            else
            {
                this.monthlySavings = null;
            }
        }

        public Money getMonthlySavings()
        {
            return monthlySavings;
        }

        public void setMonthlySavings(Money monthlySavings)
        {
            this.monthlySavings = monthlySavings;
            delegate.getWidget().getConfiguration().put(Dashboard.Config.FIRE_MONTHLY_SAVINGS.name(),
                            String.valueOf(monthlySavings.getAmount()));
            delegate.update();
            delegate.getClient().touch();
        }

        @Override
        public void menuAboutToShow(IMenuManager manager)
        {
            String display = monthlySavings != null
                            ? ((FIREWidget) delegate).formatMoneyShort(monthlySavings,
                                            delegate.getClient().getBaseCurrency())
                            : Messages.LabelFIREClickToSet;
            manager.appendToGroup(DashboardView.INFO_MENU_GROUP_NAME,
                            new LabelOnly(Messages.LabelFIREMonthlySavings + ": " + display));
        }

        @Override
        public String getLabel()
        {
            String display = monthlySavings != null
                            ? ((FIREWidget) delegate).formatMoneyShort(monthlySavings,
                                            delegate.getClient().getBaseCurrency())
                            : Messages.LabelFIREClickToSet;
            return Messages.LabelFIREMonthlySavings + ": " + display;
        }

    }

    private static class FIREReturnsConfig implements WidgetConfig
    {
        private final WidgetDelegate<?> delegate;
        private Double returns;

        public FIREReturnsConfig(WidgetDelegate<?> delegate)
        {
            this.delegate = delegate;

            String returnsStr = delegate.getWidget().getConfiguration().get(Dashboard.Config.FIRE_RETURNS.name());
            if (returnsStr != null && !returnsStr.isEmpty())
            {
                try
                {
                    this.returns = Double.parseDouble(returnsStr);
                }
                catch (NumberFormatException e)
                {
                    this.returns = null;
                }
            }
            else
            {
                this.returns = null;
            }
        }

        public Double getReturns()
        {
            return returns;
        }

        public void setReturns(Double returns)
        {
            this.returns = returns;
            delegate.getWidget().getConfiguration().put(Dashboard.Config.FIRE_RETURNS.name(), String.valueOf(returns));
            delegate.update();
            delegate.getClient().touch();
        }

        @Override
        public void menuAboutToShow(IMenuManager manager)
        {
            String display = returns != null ? Values.Percent2.format(returns) : Messages.LabelFIREClickToSet;
            manager.appendToGroup(DashboardView.INFO_MENU_GROUP_NAME,
                            new LabelOnly(Messages.LabelFIREReturns + ": " + display));
        }

        @Override
        public String getLabel()
        {
            String display = returns != null ? Values.Percent2.format(returns) : Messages.LabelFIREClickToSet;
            return Messages.LabelFIREReturns + ": " + display;
        }
    }

    private Composite container;
    private Label title;
    private ColoredLabel fireNumberLabel;
    private Text fireNumberInput;
    private ColoredLabel currentValueLabel;
    private ColoredLabel monthlySavingsLabel;
    private Text monthlySavingsInput;
    private ColoredLabel twrorLabel;
    private Text twrorInput;
    private ColoredLabel yearsToFireLabel;
    private ColoredLabel targetDateLabel;

    public FIREWidget(Widget widget, DashboardData dashboardData)
    {
        super(widget, dashboardData);

        addConfig(new FIRENumberConfig(this));
        addConfig(new FIREMonthlySavingsConfig(this));
        addConfig(new FIREReturnsConfig(this));
    }

    @Override
    public Composite createControl(Composite parent, DashboardResources resources)
    {
        container = new Composite(parent, SWT.NONE);
        container.setBackground(parent.getBackground());
        container.setData(UIConstants.CSS.CLASS_NAME, this.getContainerCssClassNames());
        GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).spacing(3, 3).applyTo(container);

        title = new Label(container, SWT.NONE);
        title.setText(TextUtil.tooltip(getWidget().getLabel()));
        title.setBackground(Colors.theme().defaultBackground());
        title.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.TITLE);
        GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(title);

        // Current Net Worth (first row)
        new Label(container, SWT.NONE); // Empty sign column
        Label currentValueLbl = new Label(container, SWT.NONE);
        currentValueLbl.setText(Messages.LabelFIRECurrentNetWorth + ":");
        currentValueLbl.setBackground(container.getBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(currentValueLbl);

        currentValueLabel = new ColoredLabel(container, SWT.RIGHT);
        currentValueLabel.setBackground(Colors.theme().defaultBackground());
        currentValueLabel.setText("");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(currentValueLabel);

        // FIRE Number (second row, editable when clicked)
        new Label(container, SWT.NONE); // Empty sign column
        Label fireNumberLbl = new Label(container, SWT.NONE);
        fireNumberLbl.setText(Messages.LabelFIRENumber + ":");
        fireNumberLbl.setBackground(container.getBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(fireNumberLbl);

        // Create both label and text field, initially show only label
        fireNumberLabel = new ColoredLabel(container, SWT.RIGHT);
        fireNumberLabel.setBackground(Colors.theme().defaultBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(fireNumberLabel);

        fireNumberInput = new Text(container, SWT.BORDER | SWT.RIGHT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(fireNumberInput);
        fireNumberInput.setVisible(false);
        ((org.eclipse.swt.layout.GridData) fireNumberInput.getLayoutData()).exclude = true;

        Money currentFireNumber = get(FIRENumberConfig.class).getFireNumber();
        String currency = getDashboardData().getClient().getBaseCurrency();
        if (currentFireNumber != null)
        {
            fireNumberLabel.setText(formatMoneyShort(currentFireNumber, currency));
            fireNumberInput.setText(Values.Amount.format(currentFireNumber.getAmount()));
        }
        else
        {
            fireNumberLabel.setText(Messages.LabelFIREClickToSet);
            fireNumberInput.setText("1500000"); // Default for editing
        }

        // Click on label to edit
        fireNumberLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDown(MouseEvent e)
            {
                showInput(fireNumberLabel, fireNumberInput);
            }
        });

        // Handle text input changes
        fireNumberInput.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                try
                {
                    StringToCurrencyConverter converter = new StringToCurrencyConverter(Values.Amount);
                    Long amount = converter.convert(fireNumberInput.getText());
                    Money newFireNumber = Money.of(getDashboardData().getClient().getBaseCurrency(), amount);
                    get(FIRENumberConfig.class).setFireNumber(newFireNumber);

                    // Update label display
                    String currency = getDashboardData().getClient().getBaseCurrency();
                    fireNumberLabel.setText(formatMoneyShort(newFireNumber, currency));
                }
                catch (Exception ex)
                {
                    // Invalid input, keep previous value
                }
            }
        });

        // Focus lost - hide text input
        fireNumberInput.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                showLabel(fireNumberLabel, fireNumberInput);
            }
        });

        // Enter key - finish editing
        fireNumberInput.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                {
                    showLabel(fireNumberLabel, fireNumberInput);
                }
            }
        });

        // Est. Monthly Savings (editable when clicked)
        new Label(container, SWT.NONE); // Empty sign column
        Label monthlySavingsLbl = new Label(container, SWT.NONE);
        monthlySavingsLbl.setText(Messages.LabelFIREMonthlySavings + ":");
        monthlySavingsLbl.setBackground(container.getBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(monthlySavingsLbl);

        // Create both label and text field, initially show only label
        monthlySavingsLabel = new ColoredLabel(container, SWT.RIGHT);
        monthlySavingsLabel.setBackground(Colors.theme().defaultBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(monthlySavingsLabel);

        monthlySavingsInput = new Text(container, SWT.BORDER | SWT.RIGHT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(monthlySavingsInput);
        monthlySavingsInput.setVisible(false);
        ((org.eclipse.swt.layout.GridData) monthlySavingsInput.getLayoutData()).exclude = true;

        Money currentMonthlySavings = get(FIREMonthlySavingsConfig.class).getMonthlySavings();
        if (currentMonthlySavings != null)
        {
            monthlySavingsLabel.setText(formatMoneyShort(currentMonthlySavings, currency));
            monthlySavingsInput.setText(Values.Amount.format(currentMonthlySavings.getAmount()));
        }
        else
        {
            monthlySavingsLabel.setText(Messages.LabelFIREClickToSet);
            monthlySavingsInput.setText("500000"); // Default $5000 for editing
        }

        // Click on label to edit
        monthlySavingsLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDown(MouseEvent e)
            {
                showInput(monthlySavingsLabel, monthlySavingsInput);
            }
        });

        // Handle text input changes
        monthlySavingsInput.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                try
                {
                    StringToCurrencyConverter converter = new StringToCurrencyConverter(Values.Amount);
                    Long amount = converter.convert(monthlySavingsInput.getText());
                    Money newMonthlySavings = Money.of(getDashboardData().getClient().getBaseCurrency(), amount);
                    get(FIREMonthlySavingsConfig.class).setMonthlySavings(newMonthlySavings);

                    // Update label display
                    String currency = getDashboardData().getClient().getBaseCurrency();
                    monthlySavingsLabel.setText(formatMoneyShort(newMonthlySavings, currency));
                }
                catch (Exception ex)
                {
                    // Invalid input, keep previous value
                }
            }
        });

        // Focus lost - hide text input
        monthlySavingsInput.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                showLabel(monthlySavingsLabel, monthlySavingsInput);
            }
        });

        // Enter key - finish editing
        monthlySavingsInput.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                {
                    showLabel(monthlySavingsLabel, monthlySavingsInput);
                }
            }
        });

        // Est. Returns (editable when clicked)
        new Label(container, SWT.NONE); // Empty sign column
        Label twrorLbl = new Label(container, SWT.NONE);
        twrorLbl.setText(Messages.LabelFIREReturns + ":");
        twrorLbl.setBackground(container.getBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(twrorLbl);

        // Create both label and text field, initially show only label
        twrorLabel = new ColoredLabel(container, SWT.RIGHT);
        twrorLabel.setBackground(Colors.theme().defaultBackground());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(twrorLabel);

        twrorInput = new Text(container, SWT.BORDER | SWT.RIGHT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(twrorInput);
        twrorInput.setVisible(false);
        ((org.eclipse.swt.layout.GridData) twrorInput.getLayoutData()).exclude = true;

        Double currentReturns = get(FIREReturnsConfig.class).getReturns();
        if (currentReturns != null)
        {
            twrorLabel.setText(Values.Percent2.format(currentReturns));
            twrorInput.setText(Values.Percent.format(currentReturns));
        }
        else
        {
            twrorLabel.setText(Messages.LabelFIREClickToSet);
            twrorInput.setText("7.0"); // Default 7% for editing
        }

        // Click on label to edit
        twrorLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDown(MouseEvent e)
            {
                showInput(twrorLabel, twrorInput);
            }
        });

        // Handle text input changes
        twrorInput.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                try
                {
                    String text = twrorInput.getText().replace("%", "");
                    Double returns = Double.parseDouble(text) / 100.0; // Convert
                                                                       // percentage
                                                                       // to
                                                                       // decimal
                    get(FIREReturnsConfig.class).setReturns(returns);

                    // Update label display
                    twrorLabel.setText(Values.Percent2.format(returns));
                }
                catch (Exception ex)
                {
                    // Invalid input, keep previous value
                }
            }
        });

        // Focus lost - hide text input
        twrorInput.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                showLabel(twrorLabel, twrorInput);
            }
        });

        // Enter key - finish editing
        twrorInput.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                {
                    showLabel(twrorLabel, twrorInput);
                }
            }
        });

        // Years to FIRE
        new Label(container, SWT.NONE); // Empty sign column
        Label yearsToFireLbl = new Label(container, SWT.NONE);
        yearsToFireLbl.setText(Messages.LabelFIREYearsToFIRE + ":");
        yearsToFireLbl.setBackground(container.getBackground());
        yearsToFireLbl.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.HEADING2);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(yearsToFireLbl);

        yearsToFireLabel = new ColoredLabel(container, SWT.RIGHT);
        yearsToFireLabel.setBackground(Colors.theme().defaultBackground());
        yearsToFireLabel.setText("");
        yearsToFireLabel.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.HEADING2);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(yearsToFireLabel);

        // FIRE Date
        new Label(container, SWT.NONE); // Empty sign column
        Label targetDateLbl = new Label(container, SWT.NONE);
        targetDateLbl.setText(Messages.LabelFIRETargetDate + ":");
        targetDateLbl.setBackground(container.getBackground());
        targetDateLbl.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.HEADING2);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(targetDateLbl);

        targetDateLabel = new ColoredLabel(container, SWT.RIGHT);
        targetDateLabel.setBackground(Colors.theme().defaultBackground());
        targetDateLabel.setText("");
        targetDateLabel.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.HEADING2);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(targetDateLabel);

        return container;
    }

    @Override
    public Control getTitleControl()
    {
        return title;
    }

    @Override
    public Supplier<FIREData> getUpdateTask()
    {
        return () -> {
            FIREData data = new FIREData();

            // Get user input FIRE number
            data.setFireNumber(get(FIRENumberConfig.class).getFireNumber());

            // Calculate current portfolio value using the first (default) data
            // series
            var availableSeries = getDashboardData().getDataSeriesSet().getAvailableSeries();
            if (!availableSeries.isEmpty())
            {
                // Use last 1 year for current value and returns calculation
                LocalDate now = LocalDate.now();
                Interval interval = Interval.of(now.minusYears(1), now);
                PerformanceIndex index = getDashboardData().calculate(availableSeries.get(0), interval);

                long[] totals = index.getTotals();
                if (totals.length > 0)
                {
                    data.setCurrentValue(Money.of(index.getCurrency(), totals[totals.length - 1]));
                }
            }

            // Get user input monthly savings and returns
            data.setMonthlySavings(get(FIREMonthlySavingsConfig.class).getMonthlySavings());
            Double userReturns = get(FIREReturnsConfig.class).getReturns();
            if (userReturns != null)
            {
                data.setTwror(userReturns);
            }
            else if (!availableSeries.isEmpty())
            {
                // Fallback to calculated returns if user hasn't set custom
                // value
                LocalDate now = LocalDate.now();
                Interval interval = Interval.of(now.minusYears(1), now);
                PerformanceIndex index = getDashboardData().calculate(availableSeries.get(0), interval);
                data.setTwror(index.getFinalAccumulatedAnnualizedPercentage());
            }

            // Calculate years to FIRE and target date
            if (data.getFireNumber() != null && data.getCurrentValue() != null && data.getMonthlySavings() != null
                            && data.getTwror() > 0)
            {
                double yearsToFire = calculateYearsToFIRE(data.getCurrentValue().getAmount(),
                                data.getFireNumber().getAmount(), data.getMonthlySavings().getAmount(),
                                data.getTwror());
                data.setYearsToFire(yearsToFire);

                if (yearsToFire > 0 && yearsToFire < 100) // reasonable bounds
                {
                    long daysToAdd = Math.round(yearsToFire * 365.25);
                    data.setTargetDate(LocalDate.now().plusDays(daysToAdd));
                }
            }

            return data;
        };
    }

    private double calculateYearsToFIRE(long currentValue, long fireNumber, long monthlySavings, double annualReturn)
    {
        if (currentValue >= fireNumber)
            return 0;

        if (monthlySavings <= 0)
            return Double.POSITIVE_INFINITY;

        double monthlyReturn = Math.pow(1.0 + annualReturn, 1.0 / 12.0) - 1.0;

        if (monthlyReturn <= 0)
        {
            // Simple linear calculation if no growth
            return (double) (fireNumber - currentValue) / (monthlySavings * 12);
        }

        // Future value formula: FV = PV × (1 + r)^n + PMT × [((1 + r)^n - 1) /
        // r]
        // This requires numerical solution since it can't be solved
        // algebraically
        double pv = currentValue;
        double fv = fireNumber;
        double pmt = monthlySavings;
        double r = monthlyReturn;

        // Use binary search to find n
        double low = 0;
        double high = 600; // max 50 years
        double epsilon = 0.01; // precision to 0.01 months

        while (high - low > epsilon)
        {
            double mid = (low + high) / 2.0;
            double powerTerm = Math.pow(1 + r, mid);
            double calculatedFV = pv * powerTerm + pmt * ((powerTerm - 1) / r);

            if (calculatedFV < fv)
            {
                low = mid;
            }
            else
            {
                high = mid;
            }
        }

        return (low + high) / 2.0 / 12.0;
    }

    private void showInput(ColoredLabel label, Text input)
    {
        label.setVisible(false);
        ((org.eclipse.swt.layout.GridData) label.getLayoutData()).exclude = true;

        input.setVisible(true);
        ((org.eclipse.swt.layout.GridData) input.getLayoutData()).exclude = false;

        container.layout(true);
        input.setFocus();
        input.selectAll();
    }

    private void showLabel(ColoredLabel label, Text input)
    {
        input.setVisible(false);
        ((org.eclipse.swt.layout.GridData) input.getLayoutData()).exclude = true;

        label.setVisible(true);
        ((org.eclipse.swt.layout.GridData) label.getLayoutData()).exclude = false;

        container.layout(true);
    }

    private String formatMoneyShort(Money money, String currency)
    {
        // Create a Money object with rounded amount (no cents) and format
        // normally
        long roundedAmount = (money.getAmount() / 100) * 100; // Round to
                                                              // nearest dollar
        Money roundedMoney = Money.of(currency, roundedAmount);
        return Values.Money.format(roundedMoney, currency).replaceAll("\\.00", "");
    }

    @Override
    public void update(FIREData data)
    {
        title.setText(TextUtil.tooltip(getWidget().getLabel()));

        String currency = getDashboardData().getClient().getBaseCurrency();

        if (data.getCurrentValue() != null)
        {
            currentValueLabel.setText(formatMoneyShort(data.getCurrentValue(), currency));
        }
        else
        {
            currentValueLabel.setText("-");
        }

        // Update monthly savings display only if user hasn't set a custom value
        Money userMonthlySavings = get(FIREMonthlySavingsConfig.class).getMonthlySavings();
        if (userMonthlySavings != null)
        {
            monthlySavingsLabel.setText(formatMoneyShort(userMonthlySavings, currency));
            monthlySavingsLabel.setTextColor(userMonthlySavings.isNegative() ? Colors.theme().redForeground()
                            : Colors.theme().greenForeground());
        }
        else
        {
            monthlySavingsLabel.setText(Messages.LabelFIREClickToSet);
            monthlySavingsLabel.setTextColor(Colors.theme().defaultForeground());
        }

        // Update returns display only if user hasn't set a custom value
        Double userReturns = get(FIREReturnsConfig.class).getReturns();
        if (userReturns != null)
        {
            twrorLabel.setText(Values.Percent2.format(userReturns));
            twrorLabel.setTextColor(
                            userReturns < 0 ? Colors.theme().redForeground() : Colors.theme().greenForeground());
        }
        else
        {
            twrorLabel.setText(Messages.LabelFIREClickToSet);
            twrorLabel.setTextColor(Colors.theme().defaultForeground());
        }

        if (data.getFireNumber() == null)
        {
            yearsToFireLabel.setText("-");
            yearsToFireLabel.setTextColor(Colors.theme().defaultForeground());
            targetDateLabel.setText("-");
            targetDateLabel.setTextColor(Colors.theme().defaultForeground());
        }
        else if (data.getYearsToFire() > 0 && data.getYearsToFire() < 100)
        {
            yearsToFireLabel.setText(String.format("%.1f years", data.getYearsToFire()));
            yearsToFireLabel.setTextColor(Colors.theme().defaultForeground());

            if (data.getTargetDate() != null)
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
                targetDateLabel.setText(data.getTargetDate().format(formatter));
                targetDateLabel.setTextColor(Colors.theme().defaultForeground());
            }
            else
            {
                targetDateLabel.setText("-");
            }
        }
        else if (data.getYearsToFire() == 0)
        {
            yearsToFireLabel.setText("FIRE achieved!");
            yearsToFireLabel.setTextColor(Colors.theme().greenForeground());
            targetDateLabel.setText("Today!");
            targetDateLabel.setTextColor(Colors.theme().greenForeground());
        }
        else
        {
            yearsToFireLabel.setText("∞");
            yearsToFireLabel.setTextColor(Colors.theme().redForeground());
            targetDateLabel.setText("-");
            targetDateLabel.setTextColor(Colors.theme().defaultForeground());
        }

        container.layout();
    }
}
