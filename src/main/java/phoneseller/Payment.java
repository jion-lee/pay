package phoneseller;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Double price;
    private String process;

    @PrePersist
    public void onPrePersist() {
        System.out.println("payment pre persist");
    }
    @PostPersist
    public void onPostPersist(){
        System.out.println("***** 결재 요청 *****");

        if("Ordered".equals(process)) {
            System.out.println("***** 결재 진행 *****");
            setProcess("Payed");
            PayCompleted payCompleted = new PayCompleted();
            BeanUtils.copyProperties(this, payCompleted);
            payCompleted.publish();

            System.out.println(toString());
            System.out.println("***** 결재 완료 *****");

        }
    }

    @PreUpdate
    public void onPreUpdate(){
        if("OrderCancelled".equals(process)) {
            System.out.println("***** 결재 취소 중 *****");
            setProcess("PayCancelled");
            setPrice((double) 0);
            PayCancelled payCancelled = new PayCancelled();
            BeanUtils.copyProperties(this, payCancelled);
            payCancelled.publishAfterCommit();

            //Following code causes dependency to external APIs
            // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.
            phoneseller.external.Promotion promotion = new phoneseller.external.Promotion();
            promotion.setOrderId(getOrderId());
            promotion.setPoint((double)-1);
            promotion.setProcess("PayCancelled");
            // mappings goes here
            PayApplication.applicationContext.getBean(phoneseller.external.PromotionService.class)
                    .payCancel(promotion);

            System.out.println("***** 결재 취소 완료 *****");
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", price=" + price +
                ", process='" + process + '\'' +
                '}';
    }
}
