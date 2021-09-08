package com.Bitgo.AncestralTreeCount.service;

import com.Bitgo.AncestralTreeCount.exceptions.InvalidBlockException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
@Slf4j
public class MainService {
    RestTemplate restTemplate = new RestTemplate();

    public List<Pair> getNLargestAncestrySet(int n) throws InvalidBlockException {
        List<Pair> largestCountTxns = new ArrayList<>();
        try {
            String[] txnsList = getBlockTransactions("680000");
            if (txnsList == null)
                throw new InvalidBlockException();
            Set<String> txns = new HashSet<>(Arrays.asList(txnsList));
            Map<String, Integer> countMap = new HashMap<>();
            PriorityQueue<Pair<String, Integer>> minHeap = new PriorityQueue<>(Comparator.comparingInt(Pair::getValue));
            Map<String, List<String>> relationMap = createRelationMap(txns);
            for (String s : txns) {
                if (relationMap.containsKey(s) && !countMap.containsKey(s)) {
                    dfs(countMap, relationMap, minHeap, s, n);
                }
            }

            //Below piece is just to display o/p in sorted manner
            while (!minHeap.isEmpty()) {
                Pair<String, Integer> p = minHeap.poll();
                largestCountTxns.add(p);
            }
            log.info("result: " + largestCountTxns);

        }catch (InvalidBlockException e){
            log.error("Please give a valid block. Error:"+ e);
        }catch (NullPointerException e) {
            log.error("Looks like some value is Null...  Error:" + e);
        }catch (HttpClientErrorException e){
            log.error("Bad Request!  Error:"+ e);
        }

        return largestCountTxns;
    }
    

    public String getBlockHash(String blockId){
        String blockHash ="";
        try {
            final String uri = "https://blockstream.info/api/block-height/" + blockId;
            blockHash = restTemplate.getForObject(uri, String.class);
            log.info("blockId: " + blockId);
            log.info("blockHash: " + blockHash);
        }catch (InvalidDataAccessApiUsageException e){
            log.error("Something went wrong while accessing the block hash. Error: "+e);
        }
        return  blockHash;
    }
    public String[] getBlockTransactions(String blockId){
        String[] result = null;
        try {
            String blockHash = getBlockHash(blockId);
            final String uri = "https://blockstream.info/api/block/" + blockHash + "/txids";
            result = restTemplate.getForObject(uri, String[].class);
            log.info("Transactions in Block: " + Arrays.toString(result));
        }catch (HttpClientErrorException e){
            log.error("Something went wrong while accessing the block transactions. Error: "+e);
        }
        return  result;
    }
    
    public JsonNode getTransactionForTxnId(String id){
        JsonNode result = null;
        try {
            final String uri = "https://blockstream.info/api/tx/" + id + "?someparam=" + id;
            result = restTemplate.getForObject(uri, JsonNode.class);
        }catch (HttpClientErrorException e){
            log.error("Something went wrong while accessing the transaction for id: {} . Error: {}",id,e);
        }
        return  result;
    }

//    Map of child -> List of first level parents
    public Map<String,List<String>> createRelationMap(Set<String> validKeySet){
        Map<String,List<String>> relationMap = new HashMap<>();
        int count =1;
        for(String childTxnId: validKeySet){
            if(!relationMap.containsKey(childTxnId)){
                createRelationshipMapHelper(validKeySet,relationMap,childTxnId);
            }
            log.info("Number of records Processed : "+ count);
            count++;
        }

        return relationMap;
    }

    public void createRelationshipMapHelper(Set<String> validKeySet, Map<String,List<String>> relationMap,String childTxnId){
        JsonNode txnJson = getTransactionForTxnId(childTxnId);
        JsonNode parentTxns = txnJson.get("vin");
        for(JsonNode parentTxnJson: parentTxns){
            String parentTxnId =  parentTxnJson.get("txid").asText();
            if(validKeySet.contains(parentTxnId)){
                List<String> parentList = relationMap.getOrDefault(childTxnId,new ArrayList<>());
                parentList.add(parentTxnId);
                relationMap.put(childTxnId,parentList);
                //recursive call for the parent now
                createRelationshipMapHelper(validKeySet,relationMap,parentTxnId);
            }
        }
    }



    private int dfs(Map<String, Integer> countMap, Map<String, List<String>> relationMap,PriorityQueue<Pair<String,Integer>> pq, String s,int n) {
            if(countMap.containsKey(s))
                return countMap.get(s);
            if(!relationMap.containsKey(s)){
                if(pq.size()<n)
                    pq.add(new Pair(s,1));
                countMap.put(s,1);
                return 1;
            }
            else {
                int max =Integer.MIN_VALUE;
                for(String parent : relationMap.get(s)){
                    max = Math.max(max,1+ dfs(countMap,relationMap,pq,parent,n));
                }
                if(pq.size()==n && max>pq.peek().getValue()) {
                    pq.poll();
                    pq.add(new Pair(s,max));
                }
                else if(pq.size()<n) {
                    pq.add(new Pair(s, max));
                }
                countMap.put(s,max);
                return max;
            }
    }

   

    
    
    public class Pair<K,V>{
        K key;
        V value;

        public Pair(K key,V value){
            this.key = key;
            this.value = value;
        }
        public K getKey(){
            return this.key;
        }
        public V getValue(){
            return this.value;
        }
        @Override
        public String toString(){
            return "{ txnId: "+ key+", count: "+value +" }";
        }


    }


}
