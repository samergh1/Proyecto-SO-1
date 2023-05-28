/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marti
 */
public class Employee extends Thread {
    
    private float salary;
    private int durationDay;
    private float productionPerDay;
    private PartsWarehouse partsWarehouse;
    private CarsWarehouse carsWarehouse;
    private String type;
    private float accSalary;
    private float productionCounter;
    private boolean keepGoing;
    private CarsPlant plant;
    
    private int qttyAccessories;
    private int qttyChasis;
    private int qttyMotors;
    private int qttyWheels;
    private int qttyBodyworks;

    public Employee(float salary, String type, float production, PartsWarehouse partsWarehouse, CarsWarehouse carsWarehouse, CarsPlant plant) {
        this.partsWarehouse = partsWarehouse;
        this.carsWarehouse = carsWarehouse;
        this.productionPerDay = production;
        this.type = type;
        this.accSalary = 0;
        this.productionCounter = 0;
        this.salary = salary;
        this.keepGoing = true;
        this.plant = plant;
        this.durationDay = plant.getDayDuration();
        
        if (plant.isIsBugatti()) {
            this.qttyAccessories = 2;
            this.qttyBodyworks = 2;
            this.qttyChasis = 1;
            this.qttyMotors = 4;
            this.qttyWheels = 4;
        } else {
            this.qttyAccessories = 3;
            this.qttyBodyworks = 1;
            this.qttyChasis = 1;
            this.qttyMotors = 2;
            this.qttyWheels = 4;
        }
    }

    @Override
    public void run() {
        while(this.keepGoing) {
            try {
                sleep(this.durationDay);
                productionOfDay();
                getPayment();
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Employee.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
    
    public void productionOfDay() {
        this.productionCounter += this.productionPerDay;
        
        if (this.type.equals(EmployeeTypes.assemblerEmployee)) {
            this.assembleCar();
        } else {
            if (this.productionCounter >= 1) {
                try {
                    this.partsWarehouse.getSemaphore().acquire();
                    this.partsWarehouse.updateStorage(this.type, (int) this.productionCounter);
                    this.partsWarehouse.getSemaphore().release();
                    this.productionCounter = 0;

                } catch (InterruptedException ex) {
                    Logger.getLogger(Employee.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
        }
    }
    
    public void assembleCar() {
        boolean carWithAccessories = false;
        boolean hasAccessoriesAvailable = true;
        if (this.partsWarehouse.getCarsUntilAccessories() == 0) {
            carWithAccessories = true;
        }
        
        if ((this.partsWarehouse.getBodyworksDone() >= this.qttyBodyworks)
             && (this.partsWarehouse.getChasisDone() >= this.qttyChasis)
             && (this.partsWarehouse.getMotorsDone() >= this.qttyMotors)
             && (this.partsWarehouse.getWheelsDone() >= this.qttyWheels)) {
            
            if (this.partsWarehouse.getAccessoriesDone() < this.qttyAccessories) {
                hasAccessoriesAvailable = false;
            }
            
            if (!carWithAccessories) {
                try {
                    this.partsWarehouse.getSemaphore().acquire();
                    this.partsWarehouse.setBodyworksDone((this.partsWarehouse.getBodyworksDone() - this.qttyBodyworks));
                    this.partsWarehouse.setChasisDone((this.partsWarehouse.getChasisDone() - this.qttyChasis));
                    this.partsWarehouse.setMotorsDone((this.partsWarehouse.getMotorsDone() - this.qttyMotors));
                    this.partsWarehouse.setWheelsDone((this.partsWarehouse.getWheelsDone() - this.qttyWheels));
                    this.partsWarehouse.setCarsUntilAccessories(this.partsWarehouse.getCarsUntilAccessories()- 1);
                    this.partsWarehouse.getSemaphore().release();
                    
                    sleep(this.durationDay * 2);
                    
                    this.carsWarehouse.getSemaphore().acquire();
                    this.carsWarehouse.updateStorage((int) this.productionCounter);
                    this.carsWarehouse.getSemaphore().release();

                } catch (InterruptedException ex) {
                    Logger.getLogger(Employee.class.getName()).log(Level.SEVERE, null, ex);
                } 
            } else if (hasAccessoriesAvailable) {
                try {
                    this.partsWarehouse.getSemaphore().acquire();
                    this.partsWarehouse.setAccessoriesDone((this.partsWarehouse.getAccessoriesDone() - this.qttyAccessories));
                    this.partsWarehouse.setBodyworksDone((this.partsWarehouse.getBodyworksDone() - this.qttyBodyworks));
                    this.partsWarehouse.setChasisDone((this.partsWarehouse.getChasisDone() - this.qttyChasis));
                    this.partsWarehouse.setMotorsDone((this.partsWarehouse.getMotorsDone() - this.qttyMotors));
                    this.partsWarehouse.setWheelsDone((this.partsWarehouse.getWheelsDone() - this.qttyWheels));
                    this.partsWarehouse.setCarsUntilAccessories(this.partsWarehouse.getOriginalCarsUntilAccessories());
                    this.partsWarehouse.getSemaphore().release();
                    
                    sleep(this.durationDay * 2);
                    
                    this.carsWarehouse.getSemaphore().acquire();
                    this.carsWarehouse.updateStorage((int) this.productionCounter);
                    this.carsWarehouse.getSemaphore().release();

                } catch (InterruptedException ex) {
                    Logger.getLogger(Employee.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
    }
    
    public void getPayment() {
        this.accSalary += this.salary * 24;
        this.plant.setCosts(this.plant.getCosts() + (this.salary * 24)); //Hay que revisar
    }
    
    public void stopRunning() {
        this.keepGoing = false;
    }
    
    // Getters and setters

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public int getDurationDay() {
        return durationDay;
    }

    public void setDurationDay(int durationDay) {
        this.durationDay = durationDay;
    }

    public float getProductionPerDay() {
        return productionPerDay;
    }

    public void setProductionPerDay(float productionPerDay) {
        this.productionPerDay = productionPerDay;
    }
    
    public PartsWarehouse getPartsWarehouse() {
        return partsWarehouse;
    }

    public void setPartsWarehouse(PartsWarehouse partsWarehouse) {
        this.partsWarehouse = partsWarehouse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getAccSalary() {
        return accSalary;
    }

    public void setAccSalary(float accSalary) {
        this.accSalary = accSalary;
    }

    public float getProductionCounter() {
        return productionCounter;
    }

    public void setProductionCounter(float productionCounter) {
        this.productionCounter = productionCounter;
    }

    public boolean isKeepGoing() {
        return keepGoing;
    }

    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }

    public CarsWarehouse getCarsWarehouse() {
        return carsWarehouse;
    }

    public void setCarsWarehouse(CarsWarehouse carsWarehouse) {
        this.carsWarehouse = carsWarehouse;
    }

    public CarsPlant getPlant() {
        return plant;
    }

    public void setPlant(CarsPlant plant) {
        this.plant = plant;
    }

}