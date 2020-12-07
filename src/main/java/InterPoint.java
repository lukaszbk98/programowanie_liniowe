public class InterPoint {
  private int deliver;
  private int receiver;
  private int cost;

  public InterPoint(int deliver, int receiver, int cost) {
    this.deliver = deliver;
    this.receiver = receiver;
    this.cost = cost;
  }

  public int getDeliver() {
    return deliver;
  }

  public void setDeliver(int deliver) {
    this.deliver = deliver;
  }

  public int getReceiver() {
    return receiver;
  }

  public void setReceiver(int receiver) {
    this.receiver = receiver;
  }

  public int getCost() {
    return cost;
  }

  public void setCost(int cost) {
    this.cost = cost;
  }
}
