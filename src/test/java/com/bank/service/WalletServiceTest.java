package com.bank.service;

import jakarta.transaction.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WalletServiceTest {
//    @Autowired
//    private CustomerRepository customerRepository;
//    @Autowired
//    private CurrencyRepository currencyRepository;
//    @Autowired
//    private WalletRepository walletRepository;
//    @Autowired
//    private WalletService walletService;
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    private Currency usd;
//    private Currency eur;
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @BeforeEach
//    void beforeEach() {
//        usd = currencyRepository.save(new Currency(CurrencyType.USD, BigDecimal.valueOf(1.00)));
//        eur = currencyRepository.save(new Currency(CurrencyType.EUR, BigDecimal.valueOf(1.18)));
//    }
//
//    @AfterEach
//    void afterEach() {
//        transactionRepository.deleteAll();
//        categoryRepository.deleteAll();
//        walletRepository.deleteAll();
//        customerRepository.deleteAll();
//        currencyRepository.deleteAll();
//    }
//
//    @Test
//    void testCreateWallet() {
//        Customer customer = createCustomer();
//
//        WalletRequest createRequest = WalletRequest.builder()
//                .name("My main wallet")
//                .currency(String.valueOf(usd.getName()))
//                .savings(false)
//                .customerId(customer.getId())
//                .initialValue(BigDecimal.valueOf(100))
//                .build();
//
//        Wallet wallet = walletService.addWallet(createRequest);
//
//        assert wallet != null;
//        assertThat(wallet.getName()).isEqualTo("My main wallet");
//        assertThat(wallet.getCustomer().getId()).isEqualTo(customer.getId());
//        assertThat(wallet.getCurrency().getName()).isEqualTo(CurrencyType.USD);
//        assertThat(wallet.getSavings()).isEqualTo(false);
//    }
//
//    @Test
//    void getWallet() {
//        Customer customer = createCustomer();
//        Wallet wallet = createWallet(customer, usd);
//
//        Wallet retrievedWallet = walletService.getWallet(wallet.getId());
//        assert retrievedWallet != null;
//        assertThat(retrievedWallet.getName()).isEqualTo("My wallet");
//        assertThat(retrievedWallet.getCustomer().getId()).isEqualTo(customer.getId());
//        assertThat(retrievedWallet.getCurrency().getName()).isEqualTo(CurrencyType.USD);
//    }
//
//    @Test
//    void updateWallet() {
//        Customer customer = createCustomer();
//        Wallet wallet = createWallet(customer, usd);
//
//        WalletRequest updateRequest = WalletRequest.builder()
//                .name("My updated wallet")
//                .currency(String.valueOf(eur.getName()))
//                .savings(true)
//                .currentValue(BigDecimal.valueOf(1000))
//                .build();
//
//        Wallet updatedWallet = walletService.updateWallet(wallet.getId(), updateRequest);
//
//        assert updatedWallet != null;
//        assertThat(updatedWallet.getName()).isEqualTo("My updated wallet");
//        assertThat(updatedWallet.getCustomer().getId()).isEqualTo(customer.getId());
//        assertThat(updatedWallet.getCurrency().getName()).isEqualTo(CurrencyType.EUR);
//        assertThat(updatedWallet.getSavings()).isEqualTo(true);
//        assertThat(updatedWallet.getCurrentValue()).isEqualTo(BigDecimal.valueOf(1000));
//    }
//
//    @Test
//    void deleteWallet() {
//        Customer customer = createCustomer();
//        Wallet wallet = createWallet(customer, usd);
//
//        walletService.deleteWallet(wallet.getId());
//
//        Optional<Wallet> deletedWallet = walletRepository.findById(wallet.getId());
//
//        assert deletedWallet.isEmpty();
//    }
//
//    @Test
//    void getWalletTransactions() {
//        Customer customer = createCustomer();
//        Wallet wallet = createWallet(customer, usd);
//        createTransactions(customer, wallet);
//
//        List<Transaction> transactions = walletService.getWalletTransactions(wallet.getId());
//
//        assertThat(!transactions.isEmpty());
//        assertThat(transactions.size()).isEqualTo(2);
//    }
//
//    private Customer createCustomer() {
//        Customer customer = new Customer();
//        customer.setBirthDate(new Date());
//        customer.setCreationDate(new Date());
//        customer.setBlocked(false);
//        customer.setEmail("user1@example.com");
//        customer.setRole(Role.USER);
//        customer.setUserName("user1");
//        customer.setPassword("password1");
//        customer.setProfilePicture("pic1.jpg");
//        customer.setFirstName("John");
//        customer.setLastName("Doe");
//
//        return customerRepository.save(customer);
//    }
//
//    private Wallet createWallet(Customer customer, Currency currency) {
//        Wallet wallet = new Wallet();
//        wallet.setName("My wallet");
//        wallet.setCustomer(customer);
//        wallet.setCurrency(currency);
//        wallet.setSavings(false);
//        wallet.setArchive(false);
//        wallet.setInitValue(BigDecimal.valueOf(100));
//        wallet.setCurrentValue(BigDecimal.valueOf(100));
//
//        return walletRepository.save(wallet);
//    }
//
//    private void createTransactions(Customer customer, Wallet wallet) {
//        Category c1 = Category.builder()
//                .name("TV")
//                .type(OUTCOME)
//                .predefined(true)
//                .customer(customer)
//                .build();
//
//        c1 = categoryRepository.save(c1);
//
//        Category c2 = Category.builder()
//                .name("Salary")
//                .type(INCOME)
//                .predefined(true)
//                .customer(customer)
//                .build();
//
//        c2 = categoryRepository.save(c2);
//
//        Transaction t1 = new Transaction();
//        t1.setName("TV");
//        t1.setAmount(BigDecimal.valueOf(100));
//        t1.setCategory(categoryRepository.getCategoryById(c1.getId()));
//        t1.setType(OUTCOME);
//        t1.setRecurring(false);
//        t1.setWallet(wallet);
//        t1.setCustomer(customer);
//
//        transactionRepository.save(t1);
//
//        Transaction t2 = new Transaction();
//        t2.setName("Salary");
//        t2.setAmount(BigDecimal.valueOf(10000));
//        t2.setCategory(categoryRepository.getCategoryById(c2.getId()));
//        t2.setType(INCOME);
//        t2.setRecurring(true);
//        t2.setFrequency(MONTHLY);
//        t2.setWallet(wallet);
//        t2.setCustomer(customer);
//
//        transactionRepository.save(t2);
//    }
}
