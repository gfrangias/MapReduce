import csv
import numpy as np
import matplotlib.pyplot as plt

filename = 'dataset.csv'

# Read the CSV file and convert its contents to a float array
with open(filename, 'r') as csvfile:
    csvreader = csv.reader(csvfile, delimiter=',')
    data = [[float(entry) for entry in row] for row in csvreader]

# Convert the list of lists to a NumPy array
serverLoads = np.array(data)

#Set the environment
T=7000
k=30
eta = np.sqrt(np.log(k)/T)

#Calculate mean per server
mean = np.mean(serverLoads, axis=1)
#Calculate the server with the best mean
bestServer = np.argmin(mean)

#Keep statistics in order to generate regret
cumulBest = np.zeros(T) #Cumulative score of the best server
cumulAlg = np.zeros(T) #Cumulative score achieved by the algorithm
expertsRegret = np.zeros(T) #Cumulative Regret for the Experts Env.
weights = np.ones(k) #Weights vector, updated at each timestep ready to be parsed by the next iteration
prob = np.zeros(k) #Probability vector
expertsRegretcumul = np.zeros(T)

print(serverLoads)

#Expert environment
for t in range(T):
    #Calculate probabilities for all k
    prob = weights/np.sum(weights)
    #Chose server i with probability prob(i)
    chosenServer = np.random.choice(k, p=prob)

    #Init exception avoidance
    if t>0:
        cumulAlg[t] = cumulAlg[t-1] + serverLoads[chosenServer][t] #Keep track of the cumulative scores
        cumulBest[t] = cumulBest[t-1] + serverLoads[bestServer][t]
    else:
        cumulAlg[t] = serverLoads[chosenServer][t]
        cumulBest[t] = serverLoads[bestServer][t]

    #Calculate Regret as Alg-Best because now best case is the smallest loss possible.
    expertsRegret[t] = (cumulAlg[t] - cumulBest[t])/(t+1)
    expertsRegretcumul[t] = expertsRegretcumul[t-1] + expertsRegret[t]

    #Overwrite weights vector with new values to be used in the next time step
    for j in range(k):
        weights[j] = np.power((1-eta),serverLoads[j][t]) * weights[j]


#Keep statistics in order to generate regret
cumulBest = np.zeros(T)
cumulAlg = np.zeros(T)
banditRegret = np.zeros(T) #Cumulative Regret for the Bandit Env.
weights = np.ones(k)
prob = np.zeros(k)
banditRegretcumul = np.zeros(T)


#Bandit environment
for t in range(T):
    prob = weights/np.sum(weights)
    chosenServer = np.random.choice(k, p=prob)

    if t>0:
        cumulAlg[t] = cumulAlg[t-1] + serverLoads[chosenServer][t]
        cumulBest[t] = cumulBest[t-1] + serverLoads[bestServer][t]
    else:
        cumulAlg[t] = serverLoads[chosenServer][t]
        cumulBest[t] = serverLoads[bestServer][t]

    banditRegret[t] = (cumulAlg[t] - cumulBest[t])/(t+1)
    banditRegretcumul[t] =banditRegretcumul[t-1] + banditRegret[t]
    
    #Calculate ^loss (hat)
    lossHat = serverLoads[chosenServer][t]/prob[chosenServer]
    #Overwrite weights vector with new value for the selected server to be used in the next time step
    weights[chosenServer] = np.power((1-eta),lossHat) * weights[chosenServer]


#Statistic vectors for LCB
lcb_pulls = np.zeros(k)
lcb_estimate_M = np.zeros(k) #estimated 
lcb_total_loss = np.zeros(k) #total for estimation
lcb_best_score = np.zeros(T) # cumulative reward of best arm for timestep T
lcb_alg_score = np.zeros(T) #cumulative reward for timestep T 
lcb_regret = np.zeros(T) # regret for timestep T
lcb_regret_cumul = np.zeros(T)


for t in range(1,T):
    #This is the root quantity - Diverse the denominator by a small number to avoid zero div exceptions at first pulls
    exploration_bonus = np.sqrt(np.log(T)/lcb_pulls + .00001)

    #Select the i-th bandit which has the max UCB
    chosenServer = np.argmin(lcb_estimate_M)
    loss = serverLoads[chosenServer][t]

    lcb_pulls[chosenServer] += 1 #Total number specific server has been selected
    lcb_total_loss[chosenServer] += loss #Total rewards of bandit i
    lcb_best_score[t] = lcb_best_score[t-1] + serverLoads[bestServer][t] #Cumulative best mean score over time
    lcb_alg_score[t] = lcb_alg_score[t-1] + loss #Cumulative score of algorithm over time
    lcb_estimate_M[chosenServer] = lcb_total_loss[chosenServer] / lcb_pulls[chosenServer] - exploration_bonus[chosenServer]#Based on the theory we maintain an estimate mu for each bandit i
    #Regret over time is the c.best reward minus the c.score that the algorithm got
    lcb_regret[t] = (lcb_alg_score[t] - lcb_best_score[t])/(t+1)
    lcb_regret_cumul[t] = lcb_regret_cumul[t-1] + lcb_regret[t]


plt.figure(1)
plt.title("Regret for T=" +str(T)+ " rounds and k=" +str(k)+ " servers") 
plt.xlabel("Round T") 
plt.ylabel("Total Regret")
plt.plot(np.arange(1,T+1),expertsRegret, label='Experts Env Regret') 
plt.plot(np.arange(1,T+1),banditRegret, label='Bandits Env Regret') 
plt.plot(np.arange(1,T+1),lcb_regret, label='LCB') 
plt.legend()
plt.show()

plt.figure(2)
plt.title("Cumulative Regret for T=" +str(T)+ " rounds and k=" +str(k)+ " servers") 
plt.xlabel("Round T") 
plt.ylabel("Total Regret")
plt.plot(np.arange(1,T+1),expertsRegretcumul, label='Experts Env Regret') 
plt.plot(np.arange(1,T+1),banditRegretcumul, label='Bandits Env Regret') 
plt.plot(np.arange(1,T+1),lcb_regret_cumul, label='LCB') 
plt.legend()
plt.show()