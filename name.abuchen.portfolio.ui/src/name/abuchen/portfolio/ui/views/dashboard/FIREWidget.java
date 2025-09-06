package name.abuchen.portfolio.ui.views.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Supplier;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Dashboard;
import name.abuchen.portfolio.model.Dashboard.Widget;
import name.abuchen.portfolio.money.CurrencyConverter;
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
                    this.fireNumber = Money.of(delegate.getClient().getBaseCurrency(), 1500000_00); // Default $1,500,000
                }
            }
            else
            {
                this.fireNumber = Money.of(delegate.getClient().getBaseCurrency(), 1500000_00); // Default $1,500,000
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
            manager.appendToGroup(DashboardView.INFO_MENU_GROUP_NAME, 
                            new LabelOnly(Messages.LabelFIRENumber + ": " + Values.Money.format(fireNumber)));
        }

        @Override
        public String getLabel()
        {
            return Messages.LabelFIRENumber + ": " + Values.Money.format(fireNumber);
        }
    }

    private Composite container;
    private Label title;
    private Text fireNumberInput;
    private ColoredLabel currentValueLabel;
    private ColoredLabel monthlySavingsLabel;
    private ColoredLabel twrorLabel;
    private ColoredLabel yearsToFireLabel;
    private ColoredLabel targetDateLabel;

    public FIREWidget(Widget widget, DashboardData dashboardData)
    {
        super(widget, dashboardData);

        addConfig(new DataSeriesConfig(this, false));
        addConfig(new ReportingPeriodConfig(this));
        addConfig(new ClientFilterConfig(this));
        addConfig(new FIRENumberConfig(this));
    }

    @Override
    public Composite createControl(Composite parent, DashboardResources resources)
    {
        container = new Composite(parent, SWT.NONE);
        container.setBackground(parent.getBackground());
        container.setData(UIConstants.CSS.CLASS_NAME, this.getContainerCssClassNames());
        GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).spacing(10, 5).applyTo(container);

        title = new Label(container, SWT.NONE);
        title.setText(TextUtil.tooltip(getWidget().getLabel()));
        title.setBackground(Colors.theme().defaultBackground());
        title.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.TITLE);
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(title);

        // FIRE Number input
        Label fireNumberLbl = new Label(container, SWT.NONE);
        fireNumberLbl.setText(Messages.LabelFIRENumber + ":");
        fireNumberLbl.setBackground(container.getBackground());

        fireNumberInput = new Text(container, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(fireNumberInput);
        
        Money currentFireNumber = get(FIRENumberConfig.class).getFireNumber();
        fireNumberInput.setText(Values.Amount.format(currentFireNumber.getAmount()));
        
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
                }
                catch (Exception ex)
                {
                    // Invalid input, keep previous value
                }
            }
        });

        // Current Value
        Label currentValueLbl = new Label(container, SWT.NONE);
        currentValueLbl.setText(Messages.LabelFIRECurrentValue + ":");
        currentValueLbl.setBackground(container.getBackground());

        currentValueLabel = new ColoredLabel(container, SWT.NONE);
        currentValueLabel.setBackground(Colors.theme().defaultBackground());
        currentValueLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(currentValueLabel);

        // Monthly Savings
        Label monthlySavingsLbl = new Label(container, SWT.NONE);
        monthlySavingsLbl.setText(Messages.LabelFIREMonthlySavings + ":");
        monthlySavingsLbl.setBackground(container.getBackground());

        monthlySavingsLabel = new ColoredLabel(container, SWT.NONE);
        monthlySavingsLabel.setBackground(Colors.theme().defaultBackground());
        monthlySavingsLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(monthlySavingsLabel);

        // TWRoR
        Label twrorLbl = new Label(container, SWT.NONE);
        twrorLbl.setText(Messages.LabelFIRETWRoR + ":");
        twrorLbl.setBackground(container.getBackground());

        twrorLabel = new ColoredLabel(container, SWT.NONE);
        twrorLabel.setBackground(Colors.theme().defaultBackground());
        twrorLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(twrorLabel);

        // Years to FIRE
        Label yearsToFireLbl = new Label(container, SWT.NONE);
        yearsToFireLbl.setText(Messages.LabelFIREYearsToFIRE + ":");
        yearsToFireLbl.setBackground(container.getBackground());

        yearsToFireLabel = new ColoredLabel(container, SWT.NONE);
        yearsToFireLabel.setBackground(Colors.theme().defaultBackground());
        yearsToFireLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(yearsToFireLabel);

        // Target Date
        Label targetDateLbl = new Label(container, SWT.NONE);
        targetDateLbl.setText(Messages.LabelFIRETargetDate + ":");
        targetDateLbl.setBackground(container.getBackground());

        targetDateLabel = new ColoredLabel(container, SWT.NONE);
        targetDateLabel.setBackground(Colors.theme().defaultBackground());
        targetDateLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(targetDateLabel);

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

            // Calculate current portfolio value
            PerformanceIndex index = getDashboardData().calculate(get(DataSeriesConfig.class).getDataSeries(),
                            get(ReportingPeriodConfig.class).getReportingPeriod().toInterval(LocalDate.now()));
            
            long[] totals = index.getTotals();
            if (totals.length > 0)
            {
                data.setCurrentValue(Money.of(index.getCurrency(), totals[totals.length - 1]));
                
                // Calculate annualized TWRoR
                data.setTwror(index.getFinalAccumulatedAnnualizedPercentage());
            }

            // Calculate monthly savings (performance-neutral transfers) using the same logic as MonthlyPNTransfersWidget
            data.setMonthlySavings(calculateMonthlySavings());

            // Calculate years to FIRE and target date
            if (data.getCurrentValue() != null && data.getMonthlySavings() != null && data.getTwror() > 0)
            {
                double yearsToFire = calculateYearsToFIRE(data.getCurrentValue().getAmount(),
                                data.getFireNumber().getAmount(), data.getMonthlySavings().getAmount(), data.getTwror());
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

    private Money calculateMonthlySavings()
    {
        Interval interval = get(ReportingPeriodConfig.class).getReportingPeriod().toInterval(LocalDate.now());
        Client filteredClient = get(ClientFilterConfig.class).getSelectedFilter()
                        .filter(getDashboardData().getClient());
        CurrencyConverter converter = getDashboardData().getCurrencyConverter();

        long totalTransfers = 0;

        // Cash account transactions
        totalTransfers += filteredClient.getAccounts().stream()
                        .flatMap(account -> account.getTransactions().stream())
                        .filter(t -> interval.contains(t.getDateTime()))
                        .mapToLong(t -> {
                            long value = 0;
                            switch (t.getType())
                            {
                                case DEPOSIT:
                                    value = t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                case REMOVAL:
                                    value = -t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                case TRANSFER_IN:
                                    value = t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                case TRANSFER_OUT:
                                    value = -t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                default:
                                    return 0;
                            }
                            return value;
                        }).sum();

        // Securities account transactions
        totalTransfers += filteredClient.getPortfolios().stream()
                        .flatMap(portfolio -> portfolio.getTransactions().stream())
                        .filter(t -> interval.contains(t.getDateTime()))
                        .mapToLong(t -> {
                            long value = 0;
                            switch (t.getType())
                            {
                                case DELIVERY_INBOUND:
                                    value = t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                case DELIVERY_OUTBOUND:
                                    value = -t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                case TRANSFER_IN:
                                    value = t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                case TRANSFER_OUT:
                                    value = -t.getMonetaryAmount().with(converter.at(t.getDateTime())).getAmount();
                                    break;
                                default:
                                    return 0;
                            }
                            return value;
                        }).sum();

        // Convert to monthly average
        long monthsInPeriod = interval.getDays() / 30; // approximate
        if (monthsInPeriod == 0) monthsInPeriod = 1;
        long monthlySavings = totalTransfers / monthsInPeriod;

        return Money.of(getDashboardData().getClient().getBaseCurrency(), monthlySavings);
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
            return (double)(fireNumber - currentValue) / (monthlySavings * 12);
        }

        // Future value formula: FV = PV × (1 + r)^n + PMT × [((1 + r)^n - 1) / r]
        // This requires numerical solution since it can't be solved algebraically
        double pv = currentValue;
        double fv = fireNumber;
        double pmt = monthlySavings;
        double r = monthlyReturn;

        // Use binary search to find n
        double low = 0;
        double high = 600; // max 50 years
        double epsilon = 0.01; // precision to 0.01 months
        
        while (high - low > epsilon) {
            double mid = (low + high) / 2.0;
            double powerTerm = Math.pow(1 + r, mid);
            double calculatedFV = pv * powerTerm + pmt * ((powerTerm - 1) / r);
            
            if (calculatedFV < fv) {
                low = mid;
            } else {
                high = mid;
            }
        }
        
        return (low + high) / 2.0 / 12.0;
    }

    @Override
    public void update(FIREData data)
    {
        title.setText(TextUtil.tooltip(getWidget().getLabel()));

        String currency = getDashboardData().getClient().getBaseCurrency();

        if (data.getCurrentValue() != null)
        {
            currentValueLabel.setText(Values.Money.format(data.getCurrentValue(), currency));
        }
        else
        {
            currentValueLabel.setText("-");
        }

        if (data.getMonthlySavings() != null)
        {
            monthlySavingsLabel.setText(Values.Money.format(data.getMonthlySavings(), currency));
            monthlySavingsLabel.setTextColor(data.getMonthlySavings().isNegative() ? 
                            Colors.theme().redForeground() : Colors.theme().greenForeground());
        }
        else
        {
            monthlySavingsLabel.setText("-");
        }

        if (data.getTwror() != 0)
        {
            twrorLabel.setText(Values.Percent2.format(data.getTwror()));
            twrorLabel.setTextColor(data.getTwror() < 0 ? 
                            Colors.theme().redForeground() : Colors.theme().greenForeground());
        }
        else
        {
            twrorLabel.setText("-");
        }

        if (data.getYearsToFire() > 0 && data.getYearsToFire() < 100)
        {
            yearsToFireLabel.setText(String.format("%.1f years", data.getYearsToFire()));
            yearsToFireLabel.setTextColor(Colors.theme().defaultForeground());
        }
        else if (data.getYearsToFire() == 0)
        {
            yearsToFireLabel.setText("FIRE achieved!");
            yearsToFireLabel.setTextColor(Colors.theme().greenForeground());
        }
        else
        {
            yearsToFireLabel.setText("∞");
            yearsToFireLabel.setTextColor(Colors.theme().redForeground());
        }

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

        container.layout();
    }
}